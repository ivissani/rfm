package test.ar.uba.dc.rfm.paralloy.scalaframework.lifters

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ar.uba.dc.rfm.minisat.intseq
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.parsers.DIMACSParser
import scala.io.Source
import ar.uba.dc.rfm.paralloy.scalaframework.lifters.VarActivityLifter
import java.io.File

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class VarActivityLifterSuite extends FunSuite {
  test("VarActivityLifter get_assumed") {
	  System.loadLibrary("minisat")
	  
	  var aux = new Minisat
	  // Remember you need to increase java stack size in order for this to work
	  // since the native method uses a stack buffer of 1MB
	  aux.read("src/test/data/pamela9.cnf")
	  val vars = aux.nVars()
	  aux.finalize()
	  aux = null
	  System.gc()
	  
	  Console printf ("vars = %d\n", vars)
	  var count = 0
	  def f() = synchronized {
		  count += 1
	  }
	  
	  List.range(1, vars+1).par.foreach( v => for(factor <- 1 :: -1 :: Nil) {
	      f()
	      Console printf ("count = %d\n", count)
	      
	      var m = new Minisat
	      m.read("src/test/data/pamela9.cnf")
		  m.prepare_for_solving(Nil, Nil, List(factor * v), Nil)
		  
		  var is = new intseq
		  m.get_assumptions(is)
		  assert(is.size == 1)
		  assert(is.get(0) == factor * v)
		  m.finalize()
		  m = null
		  System.gc()
	    }
	  )
  }
  
	test("VarActivityLifter simple test") {
	  System.loadLibrary("minisat")
	  
	  var aux = new Minisat
	  // Remember you need to increase java stack size in order for this to work
	  // since the native method uses a stack buffer of 1MB
	  aux.read("src/test/data/pamela9.cnf")
	  val vars = aux.nVars()
	  
	  for(v <- List.range(1, vars+1))
	    for(factor <- 1 :: -1 :: Nil)
	    {
	      var m = new Minisat
	      m.read("src/test/data/pamela9.cnf")
		  m.prepare_for_solving(Nil, Nil, List(factor * v), Nil)
		  
		  val ex = m.solve_time_restricted(5d, -1, -1)
		  object va extends VarActivityLifter(5)
		  
		  def f = va.variablesToLift(1)
		  val res = f(m)
		  Console println res
		  
		  Console println res.size
		  
		  assert(res.size == 5)
	      
	      Console println v
		  assert(!res.contains(v))
		  assert(!res.contains(-v))
	    }
	}
}