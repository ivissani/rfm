/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.4
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */


public class Solver {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  public Solver(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Solver obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        minisatJNI.delete_Solver(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Solver() {
    this(minisatJNI.new_Solver(), true);
  }

  public boolean simplify() {
    return minisatJNI.Solver_simplify(swigCPtr, this);
  }

  public boolean solve() {
    return minisatJNI.Solver_solve__SWIG_0(swigCPtr, this);
  }

  public boolean okay() {
    return minisatJNI.Solver_okay(swigCPtr, this);
  }

  public void add_learnt(intseq v, float act) {
    minisatJNI.Solver_add_learnt(swigCPtr, this, intseq.getCPtr(v), v, act);
  }

  public void toDimacs(String f, intseq as) {
    minisatJNI.Solver_toDimacs(swigCPtr, this, f, intseq.getCPtr(as), as);
  }

  public clauseseq get_learnts(clauseseq to) {
    return new clauseseq(minisatJNI.Solver_get_learnts(swigCPtr, this, clauseseq.getCPtr(to), to), false);
  }

  public void prueba(String pathname) {
    minisatJNI.Solver_prueba(swigCPtr, this, pathname);
  }

  public boolean read(String pathname) {
    return minisatJNI.Solver_read(swigCPtr, this, pathname);
  }

  public void add_vars(int upto) {
    minisatJNI.Solver_add_vars(swigCPtr, this, upto);
  }

  public boolean add_clause(intseq lits) {
    return minisatJNI.Solver_add_clause(swigCPtr, this, intseq.getCPtr(lits), lits);
  }

  public boolean solve(intseq assumptions) {
    return minisatJNI.Solver_solve__SWIG_1(swigCPtr, this, intseq.getCPtr(assumptions), assumptions);
  }

  public char solve_limited(intseq assumptions) {
    return minisatJNI.Solver_solve_limited(swigCPtr, this, intseq.getCPtr(assumptions), assumptions);
  }

  public void set_conf_budget(long conflicts) {
    minisatJNI.Solver_set_conf_budget(swigCPtr, this, conflicts);
  }

  public void set_prop_budget(long propagations) {
    minisatJNI.Solver_set_prop_budget(swigCPtr, this, propagations);
  }

  public void set_time_budget(double seconds) {
    minisatJNI.Solver_set_time_budget(swigCPtr, this, seconds);
  }

  public void set_budget_off() {
    minisatJNI.Solver_set_budget_off(swigCPtr, this);
  }

  public char eval(int lit) {
    return minisatJNI.Solver_eval(swigCPtr, this, lit);
  }

  public char eval_model(int lit) {
    return minisatJNI.Solver_eval_model(swigCPtr, this, lit);
  }

  public intseq conflict() {
    return new intseq(minisatJNI.Solver_conflict(swigCPtr, this), true);
  }

  public int num_vars() {
    return minisatJNI.Solver_num_vars(swigCPtr, this);
  }

  public int num_freevars() {
    return minisatJNI.Solver_num_freevars(swigCPtr, this);
  }

  public int num_assigns() {
    return minisatJNI.Solver_num_assigns(swigCPtr, this);
  }

  public int num_clauses() {
    return minisatJNI.Solver_num_clauses(swigCPtr, this);
  }

  public int num_learnts() {
    return minisatJNI.Solver_num_learnts(swigCPtr, this);
  }

}
