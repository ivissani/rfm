package ar.uba.dc.rfm.paralloy.scalaframework.filters

import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.minisat.clauseseq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.ClauseSeq

class PercentageActivityFilter(percentage : Float, lessActive : Boolean = false) extends AbstractFilter {

  def clausesToKeep() = (m : Minisat) => {
    val all = ClauseSeq.getLearntsFromMinisat(m).toList
    all.sort((a, b) => ((a.activity < b.activity) != lessActive)).take((all.size.toFloat * percentage).ceil.toInt)
  }

  def getCannonicalAndParameterizedName() : String = getClass().getCanonicalName() + "(percentage=%f, lessActive=%c)".format(percentage * 100, if(lessActive) 'T' else 'F') 

}