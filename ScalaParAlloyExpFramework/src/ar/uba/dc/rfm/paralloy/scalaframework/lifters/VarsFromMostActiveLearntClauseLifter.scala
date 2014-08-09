package ar.uba.dc.rfm.paralloy.scalaframework.lifters

import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.ClauseSeq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause

class VarsFromMostActiveLearntClauseLifter(limit : Int, sizeLimit : Int = 1024) extends AbstractLifter {

  def variablesToLift(level: Int): (Minisat ⇒ List[Int]) = { 
    def f(m : Minisat) : List[Int] = {
      val cs = ClauseSeq.getLearntsFromMinisat(m)
      var learnt = LearntClause(-1.0, null, 0)
      cs.foreach(lc => learnt = if((lc.activity > learnt.activity) && (lc.clause.literals.length <= sizeLimit)) lc else learnt)
      learnt.clause.literals.take(limit)
    }
    
    f
  }

  def getCannonicalAndParameterizedName(): String = {
    this.getClass().getCanonicalName() + "(limit = %d, sizeLimit = %d)".format(limit, sizeLimit)
  }

}