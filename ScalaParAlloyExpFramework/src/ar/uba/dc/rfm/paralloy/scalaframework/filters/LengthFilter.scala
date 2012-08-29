package ar.uba.dc.rfm.paralloy.scalaframework.filters
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.ClauseSeq

class LengthFilter(length : Int, min : Boolean = false, keepBinary : Boolean = false) extends AbstractFilter {

  def clausesToKeep() : (Minisat => List[LearntClause]) = { 
    def ret(m : Minisat) : List[LearntClause] = {
      val l = ClauseSeq.getLearntsFromMinisat(m)
      l.filter(
          learnt => (if(min) learnt.clause.literals.length >= length else learnt.clause.literals.length <= length) || (if(keepBinary) learnt.clause.literals.size <= 2 else false)
      ).toList
    }
    
    ret
  }

  def getCannonicalAndParameterizedName() : String = { 
    this.getClass().getCanonicalName() + "(length = %d, min = %c, keepBinary = %c)".format(length, if(min) 'T' else 'F', if(keepBinary) 'T' else 'F')
  }

}