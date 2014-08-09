package ar.uba.dc.rfm.paralloy.scalaframework.datatypes
import ar.uba.dc.rfm.minisat.clauseseq
import ar.uba.dc.rfm.minisat.ClauseInfo
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat

import scala.collection.mutable.MutableList

class ClauseSeq extends Iterable[LearntClause] {
	var list = new MutableList[LearntClause]
	
	def this(base : clauseseq) {
	  this()
	  

	  for(i <- List.range(0, base.size.toInt)) {
	    var is = base.get(i)
	    list += LearntClause(is.getActivity(), new IntSeq(is.getLiterals).toClause, is.getLBD())
	  }
	}
	
	def iterator() = list.iterator
//	def foreach[U](f : LearntClause => U) {
//	  list.foreach(f)
//	} 
}

object ClauseSeq {
  def getLearntsFromMinisat(m : Minisat) : ClauseSeq = {
    var cs = new clauseseq
    m.get_learnts(cs)
    new ClauseSeq(cs)
  }
}