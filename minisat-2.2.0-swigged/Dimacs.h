/****************************************************************************************[Dimacs.h]
Copyright (c) 2003-2006, Niklas Een, Niklas Sorensson
Copyright (c) 2007-2010, Niklas Sorensson

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
**************************************************************************************************/

#ifndef Minisat_Dimacs_h
#define Minisat_Dimacs_h

#include <stdio.h>

#include "ParseUtils.h"
#include "SolverTypes.h"

namespace Minisat {

//=================================================================================================
// DIMACS Parser:

/////////////////
// BEGIN NACHO //
/////////////////
template<class B, class Solver>
static void readAllLearnt(Solver & S, B& learntFile) {
	int learnts = 0;
	learnts = parseInt(learntFile);
	fprintf(stderr, "learnts=%d\n", learnts);

	vec<Lit> learnt_clause;
	for(int i = 0; i < learnts; i++) {
		float act = parseFloat(learntFile);

		readClause(learntFile, S, learnt_clause);

        char c = *learntFile;
        if(c != '@')
            fprintf(stderr, "PARSE ERROR! Unexpected char: %c\n", c), exit(3);
    
        ++learntFile;
        uint32_t lbd = parseInt(learntFile);

		S.addLearnt(learnt_clause, act, lbd);
	}
}

template<class B, class Solver>
static void readMoreActive(Solver & S, B& learntFile) {
	int learnts = 0;
	learnts = parseInt(learntFile);
	fprintf(stderr, "learnts=%d\n", learnts);

	learnts = ((double) S.learntParam / 100) * learnts;

	vec<Lit> learnt_clause;
	for(int i = 0; i < learnts; i++) {
		float act = parseFloat(learntFile);

		readClause(learntFile, S, learnt_clause);
		S.addLearnt(learnt_clause, act);
	}
}

template<class B, class Solver>
static void readLessActive(Solver & S, B& learntFile) {
	int learnts = 0;
	learnts = parseInt(learntFile);
	fprintf(stderr, "learnts=%d\n", learnts);

	int skip_learnts = ((double) (100 - S.learntParam) / 100) * learnts;
	fprintf(stderr, "Skipped = %d", skip_learnts);

	vec<Lit> learnt_clause;
    float act = 0.0;
	for(int i = 0; i < skip_learnts; i++) {
		act = parseFloat(learntFile);

		readClause(learntFile, S, learnt_clause);
		// S.addLearnt(learnt_clause, act);
	}
	for(int i = 0; i < learnts - skip_learnts; i++) {
		act = parseFloat(learntFile);

		readClause(learntFile, S, learnt_clause);
		S.addLearnt(learnt_clause, act);
	}
}

template<class B, class Solver>
static void readSmallerThan(Solver & S, B& learntFile) {
	int learnts = 0;
	learnts = parseInt(learntFile);
	fprintf(stderr, "learnts=%d\n", learnts);

	vec<Lit> learnt_clause;
	for(int i = 0; i < learnts; i++) {
		float act = parseFloat(learntFile);

		readClause(learntFile, S, learnt_clause);
		if(learnt_clause.size() < S.learntParam) {
			S.addLearnt(learnt_clause, act);
		}
	}
}

template<class B, class Solver>
static void readActivitiesAndPolarities(Solver & S, B & inActs)
{
	int variables = parseInt(inActs);

	for(int i = 0; i < variables; i++)
	{
		++inActs;
		char c = *inActs;

		if(c != '+' && c != '-')
                	printf("PARSE ERROR! Unexpected char: \"%c\" in iteration %d\n", *inActs, i), exit(3);

		++inActs;
		skipWhitespace(inActs);

		bool pol = (c == '+');
		double act = parseDouble(inActs);

		S.setPolarity(i, pol);
		S.setActivity(i, act);
	}
}


template<class B, class Solver>
static void readLearntClauses(Solver & S, B& learntFile) {
	
	switch(S.learntMode) {
		case 0:
			break;
        default:
            readAllLearnt(S, learntFile);
    }
    /*
		case 1:
			readAllLearnt(S, learntFile);
			break;
		case 2:
			readMoreActive(S, learntFile);
			break;
		case 3:
			readLessActive(S, learntFile);
			break;
		case 4:
			readSmallerThan(S, learntFile);
			break;
		default:
			fprintf(stderr, "Wrong learnt mode\n");
			break;
	}*/
	fprintf(stderr, "--- END READING LEARNT CLAUSES ---\n");
}

///////////////
// END NACHO //
///////////////

template<class B, class Solver>
static void readClause(B& in, Solver& S, vec<Lit>& lits) {
    int     parsed_lit, var;
    lits.clear();
    for (;;){
        parsed_lit = parseInt(in);
        if (parsed_lit == 0) break;
        var = abs(parsed_lit)-1;
        while (var >= S.nVars()) S.newVar();
        lits.push( (parsed_lit > 0) ? mkLit(var) : ~mkLit(var) );
    }
}

template<class B, class Solver>
static void parse_DIMACS_main(B& in, Solver& S) {
    vec<Lit> lits;
    int vars    = 0;
    int clauses = 0;
    int cnt     = 0;
    for (;;){
        skipWhitespace(in);
        if (*in == EOF) break;
        else if (*in == 'p'){
            if (eagerMatch(in, "p cnf")){
                vars    = parseInt(in);
                clauses = parseInt(in);
                // SATRACE'06 hack
                // if (clauses > 4000000)
                //     S.eliminate(true);
            }else{
                printf("PARSE ERROR! Unexpected char: %c\n", *in), exit(3);
            }
        } else if (*in == 'c' || *in == 'p')
            skipLine(in);
        else{
            cnt++;
            readClause(in, S, lits);
            S.addClause_(lits); }
    }
    if (vars != S.nVars())
        fprintf(stderr, "WARNING! DIMACS header mismatch: wrong number of variables.\n");
    if (cnt  != clauses)
        fprintf(stderr, "WARNING! DIMACS header mismatch: wrong number of clauses.\n");
}

// Inserts problem into solver.
//
template<class Solver>
static void parse_DIMACS(gzFile input_stream, Solver& S) {
    StreamBuffer in(input_stream);
    parse_DIMACS_main(in, S); }

//=================================================================================================
}

#endif
