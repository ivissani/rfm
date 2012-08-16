package ar.uba.dc.rfm.paralloy.scalaframework.datatypes
import ar.uba.dc.rfm.minisat.intseq
import scala.collection.mutable.MutableList

class IntSeq(base : intseq) {
	def toList() : List[Int] = {
	  for(i <- List.range(0, base.size.toInt)) yield base.get(i)
	}
}