package ar.uba.dc.rfm.paralloy.scalaframework.datatypes

import ar.uba.dc.rfm.minisat.intseq

case class Clause(literals : List[Int]) {
	def toIntseq() = {
	  var is = new intseq
	  literals.foreach(is.add)
	  is
	}
}