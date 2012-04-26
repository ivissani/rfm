package test.org.rfm.paralloy.scalaframework

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.rfm.paralloy.scalaframework.Als2CnfActor
import java.io.File
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