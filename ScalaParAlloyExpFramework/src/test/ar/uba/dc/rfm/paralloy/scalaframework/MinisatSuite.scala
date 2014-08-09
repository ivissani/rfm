package test.ar.uba.dc.rfm.paralloy.scalaframework

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import scala.io.Source
import ar.uba.dc.rfm.paralloy.scalaframework.parsers.DIMACSParser
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.ClauseSeq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.IntSeq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.Clause

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class MinisatSuite extends FunSuite {
//	test("solve_restricted works properly") {
//	  //System.loadLibrary("minisat");
//	  
//	  var myMinisat = new Minisat
//	  
//	  object parser extends DIMACSParser
//	  var parserResult = parser.parseAll(parser.dimacs, Source.fromFile("src/test/data/pamela9.cnf").bufferedReader)
//
//	  var solveFor = 15.0
//	  
//	 def time[A](a: => A) = {
//		  val start = System.currentTimeMillis()
//		  val res = a
//		  val end = System.currentTimeMillis()
//		  
//		  res
//		  end - start
//	  }
//	  
//	  Console printf ("parserResult.get.clauses.size = %d\n", parserResult.get.clauses.size)
//	  myMinisat.prepare_for_solving(parserResult.get.clauses, Nil, Nil, Nil)
//	  myMinisat.simplify()
//	  
//	  var m = new Minisat
//	  m.read("src/test/data/pamela9.cnf")
//	  m.simplify()
//	  
//	  // @WARNING: It's not equivalent to parse the file from Minisat, than parsing the file outside and then adding the clauses
//	  // My guess is that it has to do with the order in which clauses are added.
//	  // The clause set becomes equivalent after simplification
//	  Console printf ("myMinisat.nClauses = %d,  m.nClauses = %d\n", myMinisat.nClauses, m.nClauses)
//	  assert(myMinisat.nClauses == m.nClauses)
//	  
//	  val cl1 = IntSeq.getClausesFromMinisat(myMinisat)
//	  val cl2 = IntSeq.getClausesFromMinisat(m)
//	  
//	  // Free some memory
//	  m.finalize()
//	  m = null
//	  System.gc()
//	  
//	  assert(cl1.map(_.literals.toSet).toSet == cl2.map(_.literals.toSet).toSet)
//	  
//	  val ex = time(myMinisat.solve_time_restricted(solveFor, -1, -1))
//	  
//	  assert((ex.toDouble/1000 - solveFor).abs < (0.1 * solveFor))
//	}
//	
//	test("learnt clauses") {
//	  var m = new Minisat
//	  m.read("src/test/data/pamela9.cnf")
//	  m.prepare_for_solving(Nil, Nil, Nil, Nil)
//	  m.solve_time_restricted(15d, -1, -1)
//	  
//	  val learnt = ClauseSeq.getLearntsFromMinisat(m)
//	  
//	  Console printf ("learnt.size = %d,  m.nLearnts = %d\n", learnt.size, m.nLearnts)
//	  assert(learnt.size == m.nLearnts)
//	  
//	  m.finalize();m = null;System.gc()
//	  
//	  m = new Minisat
//	  m.read("src/test/data/pamela9.cnf")
//	  
//	  m.prepare_for_solving(Nil, learnt.toList, Nil, Nil)
//	  val otherLearnt = ClauseSeq.getLearntsFromMinisat(m)
//	  
//	  assert(learnt.map(_.clause.literals.toSet).toSet == otherLearnt.map(_.clause.literals.toSet).toSet)
//	  
//	  m.solve_time_restricted(30d, -1, -1)
//	  assert(true)
//	}
//	
//	test("double assumption") {
//	  var m = new Minisat("src/test/data/pamela9.cnf")
//	  
//	  m.prepare_for_solving(Nil, Nil, 1 :: -1648 :: 23 :: Nil, Nil)
//	  if(m.simplify())
//		  m.solve_time_restricted(60d, -1, -1)
//	  
//	  assert(true)
//	}
	
	test("learnt facts") {
	  var m = new Minisat("src/test/data/k10.cnf")
	  m.prepare_for_solving(Nil, Nil, Nil, Nil)
	  m.simplify()
	  
	  m.solve_time_restricted(60d, -1, -1)
	  
	  Console print m.getLearntFacts.mkString(", ")
	  
	  assert(m.getLearntFacts.length > 0)
	}
}