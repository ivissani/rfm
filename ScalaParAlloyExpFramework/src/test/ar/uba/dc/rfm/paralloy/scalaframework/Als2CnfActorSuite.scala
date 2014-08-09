package test.ar.uba.dc.rfm.paralloy.scalaframework

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import ar.uba.dc.rfm.paralloy.scalaframework.Als2CnfActor
import scala.io.Source

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class Als2CnfActorSuite extends FunSuite {
	test("capturing output from als2cnf.jar works properly") {
	  def myAls2CnfActor = new Als2CnfActor
	  def f = new File("src/test/data/pamela9.als")
	  val translation = myAls2CnfActor.translate(f)
	  
	  assert(translation == Source.fromFile("src/test/data/pamela9.cnf").getLines().mkString("\n"))
	}
}