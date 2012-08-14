%define DOCSTRING
"
minisat-2.2.0 bindings for python, v0.1a8

tested with swig 1.3x and python 2.6/2.7
nico rfm.dc.uba.ar sep 2011
"
%enddef
%module(docstring=DOCSTRING) minisat
%feature("autodoc", "1");

%include "std_vector.i"

%{
#include "Solver.h"
#include "Dimacs.h"
#include "SolverTypes.h"
#include "ClauseInfo.h"

#include <zlib.h>
#include <vector>
#include <cassert>
using namespace Minisat;
%}


// Conversion helper functions (not exported)

%{


Lit int2Lit(int x)
{
        assert(x != 0);
        int v = abs(x) - 1;
        return x > 0 ? mkLit(v) : ~mkLit(v);
}

int Lit2int(Lit x)
{
        return sign(x) ? -(var(x)+1) : (var(x)+1);
}

void vec2std(const vec<Lit> &src, std::vector<int> &dst)
{
        int n = src.size();
        for(int i = 0; i < n; ++i)
                dst.push_back(Lit2int(src[i]));
}

void std2vec(const std::vector<int> &src, vec<Lit> &dst)
{
        int n = src.size();
        for(int i = 0; i < n; ++i)
                dst.push(int2Lit(src[i]));
}

%}


///////////////////////////////////////////////////////////////////////////////
%template(intseq) std::vector<int>;

class ClauseInfo {
        public:
        int getLBD();
        double getActivity();
        std::vector<int> & getLiterals();
};

#include "Solver.h"
#include <vector>

%template(clauseseq) std::vector<ClauseInfo>;

class Solver
{
        public:
        Solver();
        ~Solver();
        
        bool simplify(void);
        bool solve(void);
        bool okay(void) const;
        int  nVars();

        
        %extend
        {
                // Ojo, me parece que las stats son demasiado static
                //void print_stats(void) const { $self->printStats(); }

                void add_learnt(const std::vector<int> & v, float act)
                {
                        vec<Lit> vec;
                        std2vec(v, vec);
                        $self->addLearnt(vec, act);
                }

                double get_var_activity(int v)
                {
                        return $self->activity[v];
                }

                std::vector<int> & get_assumptions(std::vector<int> & to)
                {
                        vec2std($self->assumptions, to);
                        return to;
                }

                void toDimacs(const char * f, std::vector<int> as)
                {
                        vec<Lit> vec;
                        std2vec(as, vec);
                        $self->toDimacs(f, vec);
                }
                
                std::vector<ClauseInfo> & get_learnts(std::vector<ClauseInfo> & to ) {
                        to.clear();
                        for(int i = 0; i < $self->learnts.size(); i++)
                        {
                                Clause & c = $self->ca[$self->learnts[i]];
                                std::vector<int> v;
                                for(int j = 0; j < c.size(); j++)
                                {
                                        Lit & l = c[j];
                                        v.push_back(Lit2int(l));
                                }

                                ClauseInfo ci;
                                ci.literals = v;
                                ci.activity = c.activity();
                                ci.lbd = c.lbd();
                                to.push_back(ci);
                        }
                        return to;
                }

                void prueba(const char *pathname) {
                        char bufi[1024]; int leidos = 0;
                        fprintf(stderr, "Vamos a leer de: %s\n", pathname);
                        gzFile in = gzopen(pathname, "rb");
                        if(in == NULL) {
                                fprintf(stderr, "ERROR abriendo el archivo\n");
                        } else {
                                fprintf(stderr, "Abrimos el archivo OK\n");
                                leidos = gzread(in, bufi, sizeof(bufi));
                                fprintf(stderr, "Hemos leido %d bytes\n", leidos);
                                gzclose(in);
                                fprintf(stderr, "Cerramos el archivo OK\n");
                        }
                }

                bool read(const char *pathname)
                {
                        if($self->nVars() > 0)
                                fprintf(stderr, "Warning: reading on nonempty state\n");
                        fprintf(stderr, "Vamos a leer de: %s\n", pathname);
                        gzFile in = gzopen(pathname, "rb");
                        if(in == NULL)
                        {
                                fprintf(stderr, "ERROR abriendo el archivo\n");
                                return 0;
                        }
                        fprintf(stderr, "Abrimos el archivo\n");
                        parse_DIMACS(in, *$self);
                        fprintf(stderr, "Parseamos el contenido\n");
                        gzclose(in);
                        fprintf(stderr, "Cerramos el archivo\n");
                        return $self->simplify();
                }
        
                void add_vars(int upto)
                {
                        int v = abs(upto);
                        while($self->nVars() <= v)
                                $self->newVar();
                }
        
                bool add_clause(const std::vector<int> &lits)
                {
                        int n = lits.size();
                        vec<Lit> ps(n);
                        int vmax = -1;
                        for(int i = 0; i < n; ++i) {
                                ps[i] = int2Lit(lits[i]);
                                if(var(ps[i]) > vmax) vmax = var(ps[i]);
                        }
                        while($self->nVars() <= vmax) $self->newVar();
                        return $self->addClause_(ps);
                }
        
                bool solve(const std::vector<int> &assumptions)
                {
                        vec<Lit> assumps;
                        std2vec(assumptions, assumps);
                        return $self->solve(assumps);
                }
                
                char solve_limited(const std::vector<int> &assumptions)
                {
                        vec<Lit> assumps;
                        std2vec(assumptions, assumps);
                        lbool res = $self->solveLimited(assumps);
                        return res == l_True ? 'S' : (res == l_False ? 'U' : 'I');
                }
                
                void set_conf_budget(unsigned long conflicts)
		{ $self->setConfBudget(conflicts); }

                void set_prop_budget(unsigned long propagations)
		{ $self->setPropBudget(propagations); }

                void set_time_budget(double seconds)
                { $self->setTimeBudget(seconds); }

                void set_budget_off(void)
		{ $self->budgetOff(); }

                char eval(int lit) const
                {
                        lbool res = $self->value(int2Lit(lit));
                        return res == l_True ? '1' : (res == l_False ? '0' : '?');
                }

                char eval_model(int lit) const
                {
                        lbool res = $self->modelValue(int2Lit(lit));
                        return res == l_True ? '1' : (res == l_False ? '0' : '?');
                }

                std::vector<int> conflict()
                {
                        std::vector<int> result;
                        vec2std($self->conflict, result);
                        return result;
                }
                
                /*std::vector<int> imps(const std::vector<int> &lits)
                {
                        int n = lits.size();
                        vec<Lit> assvec, impvec;
                        for(int i = 0; i < n; ++i)
                                assvec.push(int2Lit(lits[i]));
                        if($self->implies(assvec, impvec)) {
                                n = impvec.size();
                                std::vector<int> result(n);
                                for(int i = 0; i < n; ++i)
                                        result[i] = Lit2int(impvec[i]);
                                return result;
                        } else {
                                std::vector<int> result(2);
                                result[0] = 1; result[1] = -1;
                                return result;
                        }
                }*/

                /*void trit(void)
                {
                        printf("Trail: ");
                        for(TrailIterator it = $self->trailBegin(); it != $self->trailEnd(); ++it)
                                printf("%d ", Lit2int(*it));
                        printf("\n");
                }*/


                /*
		void cliter(void)
		{
			for(ClauseIterator it = $self->clausesBegin(); it != $self->clausesEnd(); ++it)
				printf("%d ", (*it).size());
			printf("\n");
		}
		*/
                
                int num_vars(void)      const  { return $self->nVars();     }
                int num_freevars(void)  const  { return $self->nFreeVars(); }
                int num_assigns(void)   const  { return $self->nAssigns();  }
                int num_clauses(void)   const  { return $self->nClauses();  }
                int num_learnts(void)   const  { return $self->nLearnts();  }

                /*
		%immutable;
		uint64_t solves, starts, decisions, rnd_decisions, propagations, conflicts;
		uint64_t dec_vars, num_clauses, num_learnts, clauses_literals, learnts_literals, max_literals, tot_literals;
		%mutable;
		*/
                
        }

};
