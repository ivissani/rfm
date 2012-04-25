package test.org.rfm.paralloy.scalaframework.parsers

import org.rfm.paralloy.scalaframework.parsers.DIMACSParser
import org.scalatest.FunSuite
import scala.io.Source
import org.junit.runner._
import org.rfm.paralloy.scalaframework.parsers.LearntsParser
import java.io.RandomAccessFile
import java.io.File
import java.nio.charset.Charset
import java.nio.channels.FileChannel.MapMode

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class LearntsParserSuite extends FunSuite {
  test("learnts parser can parse a learnts file") {
    object parser extends LearntsParser
    
    def parserResult = parser.parse(parser.learntsfile, Source.fromFile("src/test/data/pamela9.filtered.learnt").bufferedReader)
     
    assert(parserResult.successful)
//    assert(parserResult.get.clauses.length == 2561)
    assert(parserResult.get.clausesamount == 2561)
  }
  
  test("learnts parser can parse a BIG learnts file") {
    object parser extends LearntsParser
    
    val file = new RandomAccessFile(new File("src/test/data/pamela9.learnt"), "r")
    val buff = file.getChannel.map(MapMode.READ_ONLY, 0, file.length)
    val enc = Charset.forName("ASCII")
    val chars = enc.decode(buff)
    
    def parserResult = parser.parse(parser.learntsfile, chars)
     
    assert(parserResult.successful)
//    assert(parserResult.get.clauses.length == 51236)
    assert(parserResult.get.clausesamount == 51236)
  }
}