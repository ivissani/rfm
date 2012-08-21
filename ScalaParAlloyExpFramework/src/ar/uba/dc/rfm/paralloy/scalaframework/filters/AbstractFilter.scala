package ar.uba.dc.rfm.paralloy.scalaframework.filters

import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause

abstract class AbstractFilter {
	def clausesToKeep() : (Minisat => List[LearntClause])
	def getCannonicalAndParameterizedName() : String
}