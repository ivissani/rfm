package test.ar.uba.dc.rfm.paralloy.scalaframework.lifters

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.parsers.DIMACSParser
import scala.io.Source
import ar.uba.dc.rfm.paralloy.scalaframework.lifters.VarActivityLifter

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class VarActivityLifterSuite extends FunSuite {
	test("VarActivityLifter simple test") {
	  System.loadLibrary("minisat")
	  
	  var m = new Minisat
	  
	  object parser extends DIMACSParser
	  val parserResult = parser.parseAll(parser.dimacs, Source.fromFile("src/test/data/pamela9.cnf").bufferedReader)
	  
	  val ex = m.solve_time_restricted(parserResult.get.clauses, 5d, Nil, List(1650))
	  object v extends VarActivityLifter(5)
	  
	  val res = v.variablesToLift()(m)
	  Console println res
	  
	  assert(res.size == 5)
	}
}