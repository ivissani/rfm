package ar.uba.dc.rfm.paralloy.scalaframework

import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.Clause
import ar.uba.dc.rfm.minisat.Solver
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause
import ar.uba.dc.rfm.minisat.intseq

class Minisat extends Solver {
	var lastExStartTimeMillis : Long = 0
	var lastExEndTimeMillis : Long = 0
	
    def solve_restricted(what : List[Clause], forTime : Double, forConflicts : Int, forPropagations : Int, withLearnts : List[LearntClause], assuming : List[Int]) : Char = {
      set_budget_off()
      
	  what.foreach(a => this.add_clause(a.toIntseq))
	  withLearnts.foreach(a => a match {case LearntClause(act, cla, lbd) => this.add_learnt(cla.toIntseq, act.toFloat)})
	  
	  set_conf_budget(forConflicts)
	  set_prop_budget(forPropagations)
	  set_time_budget(forTime)
	  
	  var is = new intseq
	  assuming.foreach(is.add)
	  
	  lastExStartTimeMillis = System.currentTimeMillis()
	  def ret = solve_limited(is)
	  lastExEndTimeMillis = System.currentTimeMillis()
	  
	  ret
	}
    
    def solve_time_restricted(what : List[Clause], forTime : Double, withLearnts : List[LearntClause], assuming : List[Int]) = solve_restricted(what, forTime, -1, -1, withLearnts, assuming)
    
    def get_last_execution_time = lastExEndTimeMillis - lastExStartTimeMillis
}