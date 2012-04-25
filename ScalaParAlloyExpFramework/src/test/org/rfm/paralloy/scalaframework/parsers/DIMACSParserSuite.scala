package test.org.rfm.paralloy.scalaframework.parsers

import org.rfm.paralloy.scalaframework.parsers.DIMACSParser
import org.scalatest.FunSuite
import scala.io.Source
import org.junit.runner._

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class DIMACSParserSuite extends FunSuite {
  test("dimacs parser can parse a dimacs file") {
    object parser extends DIMACSParser
    
    def parserResult = parser.parseAll(parser.dimacs, Source.fromFile("src/test/data/pamela9.cnf").getLines().mkString("\n"))
    
    assert(parserResult.successful)
  }
}