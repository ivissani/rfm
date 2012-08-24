package ar.uba.dc.rfm.paralloy.scalaframework.datatypes

import ar.uba.dc.rfm.minisat.intseq
import scala.collection.mutable.MutableList
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat

class IntSeq extends Traversable[Int] with Iterable[Int] {
	var list = new MutableList[Int]
	
	def toClause() = {
	  Clause(toList)
	}
	
	def this(base : intseq) {
	  this()
	  for(i <- List.range(0, base.size.toInt)) 
	    list += base.get(i)
	}
	
	def iterator = list.iterator
}

object IntSeq {
  def getClausesFromMinisat(m : Minisat) = {
    var ml = new MutableList[Clause]
    for(i <- List.range(1, m.nClauses()+1))
      ml += new IntSeq(m.get_clause(i)).toClause
    ml.toList
  }
}