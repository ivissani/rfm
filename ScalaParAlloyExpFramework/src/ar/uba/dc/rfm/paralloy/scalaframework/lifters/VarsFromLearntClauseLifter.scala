package ar.uba.dc.rfm.paralloy.scalaframework.lifters

import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.ClauseSeq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause

class VarsFromLearntClauseLifter(limit : Int = 6) extends AbstractLifter {
  assert(limit > 0)
  
  // Warning: This lifter is not deterministic since not always will return same amount of variables
  def variablesToLift(level : Int) = {
	def ret(m: Minisat): List[Int] = {
			val learnt = ClauseSeq.getLearntsFromMinisat(m)
			var minSize = 2
			while(!learnt.exists(_.clause.literals.size <= minSize)) minSize += 1
			
			val s = learnt.filter(_.clause.literals.size <= minSize).toList.sortWith(
			    (a, b) => a.activity < b.activity
			).take(limit).foldRight[List[Int]](Nil)(
			    (l, e) => l.clause.literals ::: e
			).toSet
			
			s.take(limit).toList
    }

    ret
  }
  
  def getCannonicalAndParameterizedName() = {
    val res = getClass().getCanonicalName()
    res + "(limit=%d)".format(limit)
  }

}