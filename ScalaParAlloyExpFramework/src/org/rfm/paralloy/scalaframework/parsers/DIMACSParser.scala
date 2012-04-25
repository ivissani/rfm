package org.rfm.paralloy.scalaframework.parsers

import scala.util.parsing.combinator._

class DIMACSParser extends JavaTokenParsers {
	val variable = """[1-9][0-9]*""".r ^^ (_.toInt)
	val literal = """-{0,1}""".r~variable
	val clause = rep("""\s*""".r~literal~"""\s*""".r)~"""0\s*""".r
	val amount = decimalNumber ^^ (_.toInt)
	val header = """^\s*p\s*cnf\s*""".r~amount~"""\s*""".r~amount
	val dimacs = header~rep(clause)
}