package test.ar.uba.dc.rfm.paralloy.scalaframework.datatypes

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import ar.uba.dc.rfm.paralloy.scalaframework.parsers.DIMACSParser
import scala.io.Source
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.minisat.clauseseq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.ClauseSeq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.IntSeq

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class ClauseSeqSuite extends FunSuite {
  test("construction from clauseseq works properly") {
    var m = new Minisat
    m.read("src/test/data/pamela9.cnf")
    
    m.solve_time_restricted(5d, -1, -1)
    var cs = new clauseseq
    m.get_learnts(cs)
    
    val CS = new ClauseSeq(cs)
    
    Console.printf("CS.size = %d, cs.size() = %d\n", CS.size, cs.size().toInt)
    assert(CS.size == cs.size().toInt)
    
    val CSList = CS.toList
    for(i <- List.range(0, CS.size)) 
    {
    	assert(CSList(i).clause.literals == new IntSeq(cs.get(i).getLiterals()).toList)
    }
  }
}