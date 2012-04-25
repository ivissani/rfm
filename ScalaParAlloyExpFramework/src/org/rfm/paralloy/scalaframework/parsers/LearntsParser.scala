package org.rfm.paralloy.scalaframework.parsers

import scala.util.parsing.combinator.JavaTokenParsers
import org.rfm.paralloy.scalaframework.datatypes.Clause
import org.rfm.paralloy.scalaframework.datatypes.LearntClause
import org.rfm.paralloy.scalaframework.datatypes.LearntsFile

class LearntsParser extends JavaTokenParsers {
	val variable = """[1-9][0-9]*""".r ^^ (_.toInt)
	val literal = """[\+-]{0,1}""".r~variable ^^ (a => (if(a._1 == "-") -1 else 1) * a._2) 
	val clause = rep(literal)~"""0""".r ^^ (a => Clause(a._1))
	val activity = floatingPointNumber ^^ (_.toDouble)
	val lbd = decimalNumber ^^ (_.toInt)
	val learntclause = activity~"""\s*""".r~clause~"""\s*@""".r~lbd ^^ (a => LearntClause(a._1._1._1._1, a._1._1._2, a._2))
	val amount = decimalNumber ^^ (_.toInt)
	val learntsfile = amount~rep(learntclause) ^^ (a => LearntsFile(a._1, a._2))
}