package ar.uba.dc.rfm.paralloy.scalaframework.datatypes
import ar.uba.dc.rfm.minisat.clauseseq
import ar.uba.dc.rfm.minisat.ClauseInfo

class ClauseSeq(cs : clauseseq) {
	def toList : List[LearntClause] = {
	  def convert(ci : ClauseInfo) : LearntClause = LearntClause(ci.getActivity(), Clause(new IntSeq(ci.getLiterals()).toList()) , ci.getLBD())
	  
	  for(i <- List.range(0, cs.size().toInt)) yield convert(cs.get(i))
	}
}