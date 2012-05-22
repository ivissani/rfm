package org.rfm.paralloy.scalaframework

import scala.actors.Actor
import java.io.File
import java.io.PrintStream
import rfm.alloy.A2C
import java.io.StringWriter
import java.io.ByteArrayOutputStream
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution
import scala.util.parsing.combinator.JavaTokenParsers

class Als2CnfActor extends Actor {
	def act() {
	  def translateAsynchronous(alsfile : File, actor : Actor) {
		  val mainActor = Actor.self
		  
		  Actor.actor {
		    Thread.sleep(1000)
		    mainActor ! ("TRANSLATION FINISHED", alsfile, actor, translate(alsfile))
		  }
	  }
	  
	  loop {
	    react {
	      case (alsfile : File, actor : Actor) => translateAsynchronous(alsfile, actor) 
	      case ("TRANSLATION FINISHED", alsfile : File, actor : Actor, translation : String) => actor ! translation
	    }
	  }
	}
	
	def translate(f : File) : String = {
	  object headerparser extends JavaTokenParsers {
		  def amount = decimalNumber ^^ (_.toInt)
		  def header = "p cnf "~amount~amount ^^ (a => (a._1._2, a._2))
	  }
	  
	  var baos = new ByteArrayOutputStream()
	  var pstream = new PrintStream(baos, true)
	  var oldout = new PrintStream(System.out)
	  
	  System.setOut(pstream)
	  
	  a2cmain(List(f.getAbsolutePath).toArray)
	  
	  System.setOut(oldout)
	  Console.setOut(oldout)
	  
	  val lines = baos.toString().split("\n")
	  val header = headerparser.parse(headerparser.header, lines(lines.length-1))
	  val quasires = ("p cnf %d %d".format(header.get._1 - 1, header.get._2) ::lines.take(lines.length - 1).toList)
	  
	  quasires.mkString("\n")
	}
	
	def a2cmain(paramArrayOfString : Array[String]) {
	   var localCommandLine = A2C.parseCommandLine(paramArrayOfString);
	   var localA4Options = A2C.xlateCommandLine(localCommandLine);

	   var str = localCommandLine.getArgs()(0);
	   var answer : A4Solution = null
	   
	   try
	   {
		   Console.err.println("Parsing and typechecking " + str);
		   var localCompModule = CompUtil.parseEverything_fromFile(null, null, str);
		   var localCommand = localCompModule.getAllCommands().get(0);

		   Console.err.println("Translating ...");
		   answer = TranslateAlloyToKodkod.execute_command(null, localCompModule.getAllReachableSigs(), localCommand, localA4Options);
	   } catch {
	     case localException => localException.printStackTrace();
	   }
	}
}