package ar.uba.dc.rfm.paralloy.scalaframework.lifters

import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.ClauseSeq

class VarsFromLearntClauseLifter(limit : Int = 3) extends AbstractLifter {
  assert(limit > 0)
  
  def variablesToLift(level : Int) = {
	def ret(m: Minisat): List[Int] = {
			val learnt = ClauseSeq.getLearntsFromMinisat(m).filter(_.clause.literals.size <= 2)
			learnt.toList.sort((a, b) => a.activity < b.activity).take(limit).foldRight()((e, l) => e :: l)
    }

    ret
  }
  
  def getCannonicalAndParameterizedName() = {
    val res = getClass().getCanonicalName()
    res + "(limit=%d)".format(limit)
  }

}