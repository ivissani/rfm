package ar.uba.dc.rfm.paralloy.scalaframework

import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.Clause
import ar.uba.dc.rfm.minisat.Solver
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause
import ar.uba.dc.rfm.minisat.intseq

class Minisat extends Solver {
  var lastExStartTimeMillis : Long = 0
  var lastExEndTimeMillis : Long = 0

  def solve_restricted(what : List[Clause], forTime : Double, forConflicts : Int, forPropagations : Int, withLearnts : List[LearntClause], assuming : List[Int]) : Char = {
    set_budget_off() // This resets the budgets

    // Add problem and learned clauses
    // WARNING: Watch performance here, may be a memory black hole on big problems
    what.foreach(a ⇒ this.add_clause(a.toIntseq))
    withLearnts.foreach(a ⇒ a match { case LearntClause(act, cla, lbd) ⇒ this.add_learnt(cla.toIntseq, act.toFloat) })

    // Set solving limits. The solver stops if any of these limits is surpassed
    if (forConflicts >= 0)
      set_conf_budget(forConflicts)
    if (forPropagations >= 0)
      set_prop_budget(forPropagations)
    if (forTime >= 0)
      set_time_budget(forTime)

    // Translate assumptions
    var is = new intseq
    assuming.foreach(is.add)

    // Execute measuring total time
    val localLastExStartTimeMillis = System.currentTimeMillis()
    val ret = solve_limited(is)
    val localLastExEndTimeMillis = System.currentTimeMillis()

    lastExStartTimeMillis = localLastExStartTimeMillis
    lastExEndTimeMillis = localLastExEndTimeMillis
    // @TODO : translate this into an enumerated value
    ret
  }

  def solve_time_restricted(what : List[Clause], forTime : Double, withLearnts : List[LearntClause], assuming : List[Int]) = solve_restricted(what, forTime, -1, -1, withLearnts, assuming)

  def get_last_execution_time = lastExEndTimeMillis - lastExStartTimeMillis
}