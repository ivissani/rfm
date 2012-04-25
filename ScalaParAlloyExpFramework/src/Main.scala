import org.rfm.paralloy.scalaframework.parsers.LearntsParser
import scala.io.Source

object Main {

  def main(args: Array[String]): Unit = {
    object parser extends LearntsParser
    
    
    def f = Source.fromFile("src/test/data/pamela9.filtered.learnt")
    def parserResult = parser.parseAll(parser.learntsfile,  f.bufferedReader)
     
    assert(parserResult.successful)
//    assert(parserResult.get.clauses.length == 2561)
//    assert(parserResult.get.clausesamount == 2561)
  }

}