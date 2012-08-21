package test.ar.uba.dc.rfm.paralloy.scalaframework.lifters

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.parsers.DIMACSParser
import scala.io.Source
import ar.uba.dc.rfm.paralloy.scalaframework.lifters.VarActivityLifter
import java.io.File

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class VarActivityLifterSuite extends FunSuite {
	test("VarActivityLifter simple test") {
	  System.loadLibrary("minisat")
	  
	  var m = new Minisat
	  // Remember you need to increase java stack size in order for this to work
	  // since the native method uses a stack buffer of 1MB
	  m.read("src/test/data/pamela9.cnf")
	  m.prepare_for_solving(Nil, Nil, List(-1650))
	  
	  val ex = m.solve_time_restricted(5d)
	  object v extends VarActivityLifter(5)
	  
	  def f = v.variablesToLift()
	  val res = f(m)
	  Console println res
	  
	  assert(res.size == 5)
	  assert(!res.contains(1650))
	}
}