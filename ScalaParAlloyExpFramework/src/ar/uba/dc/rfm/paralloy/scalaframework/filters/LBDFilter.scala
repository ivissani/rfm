package ar.uba.dc.rfm.paralloy.scalaframework.filters
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.ClauseSeq

class LBDFilter(lbd : Int, min : Boolean = false, keepBinary : Boolean = false) extends AbstractFilter {
  assert(lbd > 0)
  
  def clausesToKeep() : (Minisat => List[LearntClause]) = { 
    def ret(m : Minisat) : List[LearntClause] = {
      val l = ClauseSeq.getLearntsFromMinisat(m)
      l.filter(
          learnt => (if(min) learnt.lbd >= lbd else learnt.lbd <= lbd) || (if(keepBinary) learnt.clause.literals.size <= 2 else false)
      ).toList
    }
    
    ret
  }

  def getCannonicalAndParameterizedName() : String = { 
    this.getClass().getCanonicalName() + "(lbd = %d, min = %c, keepBinary = %c)".format(lbd, if(min) 'T' else 'F', if(keepBinary) 'T' else 'F')
  }

}