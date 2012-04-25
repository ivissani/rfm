package org.rfm.paralloy.scalaframework.parsers

import scala.util.parsing.combinator._
import org.rfm.paralloy.scalaframework.datatypes._

class DIMACSParser extends JavaTokenParsers {
	val variable = """[1-9][0-9]*""".r ^^ (_.toInt)
	val literal = """-{0,1}""".r~variable ^^ (a => (if(a._1 == "-") -1 else 1) * a._2) 
	val clause = rep(literal)~"""0\s*""".r ^^ (a => Clause(a._1))
	val amount = decimalNumber ^^ (_.toInt)
	val header = """^\s*p\s*cnf\s*""".r~amount~"""\s*""".r~amount ^^ (a => Tuple2(a._1._1._2, a._2))
	val dimacs = header~rep(clause) ^^ (a => DIMACS(a._1._1, a._1._2, a._2))
}