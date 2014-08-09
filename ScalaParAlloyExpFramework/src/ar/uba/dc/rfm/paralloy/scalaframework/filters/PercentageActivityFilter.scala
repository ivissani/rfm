package ar.uba.dc.rfm.paralloy.scalaframework.filters

import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.minisat.clauseseq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.ClauseSeq

class PercentageActivityFilter(percentage : Float, lessActive : Boolean = false, keepBinary : Boolean = false) extends AbstractFilter {
  assert(percentage >= 0.0)
  assert(percentage <= 1.0)
  
  def clausesToKeep() = (m : Minisat) => {
    val all = ClauseSeq.getLearntsFromMinisat(m).toList
    val filtered = all.sortWith((a, b) => ((a.activity < b.activity) != lessActive)).take((all.size.toFloat * percentage).ceil.toInt)
    
    filtered ::: (if(keepBinary) all.filter(lc => lc.clause.literals.size <= 2 && !filtered.contains(lc)) else Nil)
  }

  def getCannonicalAndParameterizedName() : String = getClass().getCanonicalName() + "(percentage = %f, lessActive = %c, keepBinary = %c)".format(percentage * 100, if(lessActive) 'T' else 'F', if(keepBinary) 'T' else 'F') 

}