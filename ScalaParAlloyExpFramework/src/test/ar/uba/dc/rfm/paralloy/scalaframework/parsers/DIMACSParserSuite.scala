package test.ar.uba.dc.rfm.paralloy.scalaframework.parsers

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import ar.uba.dc.rfm.paralloy.scalaframework.parsers.DIMACSParser
import scala.io.Source

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class DIMACSParserSuite extends FunSuite {
  test("dimacs parser can parse a dimacs file") {
    object parser extends DIMACSParser
    
    def parserResult = parser.parseAll(parser.dimacs, Source.fromFile("src/test/data/pamela9.cnf").bufferedReader)
     
    assert(parserResult.successful)
    assert(parserResult.get.clauses.length == 56536)
    assert(parserResult.get.clausesamount == 56536)
    assert(parserResult.get.variablesamount == 26778)
    assert(parserResult.get.countVariables == 26778)
  }
}