package ar.uba.dc.rfm.paralloy.scalaframework

import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.Clause
import ar.uba.dc.rfm.minisat.Solver
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause
import ar.uba.dc.rfm.minisat.intseq
import scala.collection.mutable.MutableList
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.IntSeq
import ar.uba.dc.rfm.minisat.SolverWrapper

class Minisat extends SolverWrapper {
  var lastExStartTimeMillis: Long = 0
  var lastExEndTimeMillis: Long = 0
  
  def this(path : String) {
    this()
    read(path)
  }
  
  def solve_restricted(forTime: Double, forConflicts: Int, forPropagations: Int, learntsLimit : Int, currentRestarts : Int): Char = {
    Console println nClauses()
    set_budget_off() // This resets the budgets

    
    // Set solving limits. The solver stops if any of these limits is surpassed
    if (forConflicts >= 0)
      set_conf_budget(forConflicts)
    if (forPropagations >= 0)
      set_prop_budget(forPropagations)
    if (forTime >= 0)
      set_time_budget(forTime)
    if (learntsLimit >= 0)
      set_max_learnts(learntsLimit)
    else
      reset_max_learnts()
    if (currentRestarts >= 0)
      set_current_restarts(currentRestarts)

    
    // Execute measuring total time
    val localLastExStartTimeMillis = System.currentTimeMillis()
    val ret = solve_limited(new intseq)
    val localLastExEndTimeMillis = System.currentTimeMillis()

    lastExStartTimeMillis = localLastExStartTimeMillis
    lastExEndTimeMillis = localLastExEndTimeMillis
    // @TODO : translate this into an enumerated value
    ret
  }
  
  def prepare_for_solving(
      what: List[Clause], 
      withLearnts: List[LearntClause], 
      assuming: List[Int],
      learntFacts : List[Int]) 
  {
    // Add problem and learned clauses
    // WARNING: Watch performance here, may be a memory black hole on big problems
    what.map(_.toIntseq).foreach(add_clause)
    withLearnts.foreach(a ⇒ a match { case LearntClause(act, cla, lbd) ⇒ add_learnt(cla.toIntseq, -1f, lbd) })

    // Translate assumptions
    var is = new intseq
    assuming.foreach(is.add)
    set_assumptions(is)
    
    // Set facts
    var iss = new intseq
    learntFacts.foreach(iss.add)
    set_learnt_facts(iss)
  }

  def solve_time_restricted(forTime: Double, learntsLimit : Int, restarts : Int) = solve_restricted(forTime, -1, -1, learntsLimit, restarts)
  def solve_unrestricted() = solve_time_restricted(-1, -1, -1)
  
  def get_last_execution_time = lastExEndTimeMillis - lastExStartTimeMillis
  
  def get_clause : (Int => intseq) = {
    this.get_clause(_, new intseq)
  }
  
  def get_clauses : List[Clause] = {
    def f(e : Int) = {Clause(new IntSeq(get_clause(e)).toList)}
    for(i <- List.range(0, nClauses())) yield f(i)
  }
  
  def getLearntFacts : List[Int] = {
    var is = new intseq
    get_learnt_facts(is)
    
    new IntSeq(is).toList
  }
  
  override def finalize() {
    super.finalize()
  }
}