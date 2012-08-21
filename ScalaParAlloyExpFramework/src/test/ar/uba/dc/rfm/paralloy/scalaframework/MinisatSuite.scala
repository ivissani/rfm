package test.ar.uba.dc.rfm.paralloy.scalaframework

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import scala.io.Source
import ar.uba.dc.rfm.paralloy.scalaframework.parsers.DIMACSParser

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class MinisatSuite extends FunSuite {
	test("solve_restricted works properly") {
	  //System.loadLibrary("minisat");
	  
	  var myMinisat = new Minisat
	  
	  object parser extends DIMACSParser
	  var parserResult = parser.parseAll(parser.dimacs, Source.fromFile("src/test/data/pamela9.cnf").bufferedReader)

	  var solveFor = 15.0
	  
	 def time[A](a: => A) = {
		  val start = System.currentTimeMillis()
		  val res = a
		  val end = System.currentTimeMillis()
		  
		  res
		  end - start
	  }
	  
	  myMinisat.prepare_for_solving(parserResult.get.clauses, Nil, Nil)
	  val ex = time(myMinisat.solve_time_restricted(solveFor))
	  
	  assert((ex.toDouble/1000 - solveFor).abs < (0.1 * solveFor))
	}
}