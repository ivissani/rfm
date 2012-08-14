package ar.uba.dc.rfm.paralloy.scalaframework.datatypes
import ar.uba.dc.rfm.minisat.intseq
import scala.collection.mutable.MutableList

class IntSeq(base : intseq) {
	def toList() = {
	  var mList = new MutableList[Int]
	  Range(0, base.size().toInt).foreach(mList += base.get(_))
	  mList.toList
	}
}