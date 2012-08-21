package ar.uba.dc.rfm.paralloy.scalaframework.datatypes

import ar.uba.dc.rfm.minisat.intseq
import scala.collection.mutable.MutableList

class IntSeq extends Traversable[Int] with Iterable[Int] {
	def list = new MutableList[Int]
	
	def toClause() = {
	  Clause(toList)
	}
	
	def this(base : intseq) {
	  this()
	  for(i <- List.range(0, base.size.toInt)) list += base.get(i)
	}
	
	def iterator = list.iterator
}