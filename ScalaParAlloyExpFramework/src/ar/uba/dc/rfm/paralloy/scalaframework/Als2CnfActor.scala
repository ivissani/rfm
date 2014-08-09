package ar.uba.dc.rfm.paralloy.scalaframework

import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import rfm.alloy.A2C
import scala.actors.Actor
import scala.util.parsing.combinator.JavaTokenParsers

class Als2CnfActor extends Actor {
	def act() {
	  // Defines a new actor who will translate the ALS while
	  // the main actor continues to listen for new requests.
	  // This way this is scalable by adding more working threads.
	  def translateAsynchronous(alsfile : File, actor : Actor) {
		  val mainActor = Actor.self
		  
		  Actor.actor {
		    mainActor ! ("TRANSLATION FINISHED", alsfile, actor, translate(alsfile))
		  }
	  }
	  
	  loop {
	    react {
	      // new translation request incoming
	      case (alsfile : File, actor : Actor) => translateAsynchronous(alsfile, actor)
	      
	      // one translation is ready
	      case ("TRANSLATION FINISHED", alsfile : File, actor : Actor, translation : String) => actor ! translation
	    }
	  }
	}
	
	def translate(f : File) : String = {
	   
	  object headerparser extends JavaTokenParsers {
		  def amount = decimalNumber ^^ (_.toInt)
		  def header = "p cnf "~amount~amount ^^ (a => (a._1._2, a._2))
	  }
	  
	  // Redirect standard output so we can catch the translation
	  // Super warning, this won't work if multiple fellows are writing to stdout concurrently
	  var baos = new ByteArrayOutputStream()
	  var pstream = new PrintStream(baos, true)
	  var oldout = new PrintStream(System.out)
	  
	  System.setOut(pstream)
	  
	  // Translate!
	  val translation = a2cmain(List(f.getAbsolutePath).toArray)
	  
	  // Restore standard output
	  System.setOut(oldout)
	  Console.setOut(oldout)
	  
	  val lines = translation.split("\n") // baos.toString().split("\n")
	  val header = headerparser.parse(headerparser.header, lines(lines.length-1))
	  val quasires = ("p cnf %d %d".format(header.get._1 - 1, header.get._2) ::lines.take(lines.length - 1).toList)
	  
	  quasires.mkString("\n")
	}
	
	def a2cmain(paramArrayOfString : Array[String]) : String = {	  
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
		   return null
	   } catch {
	     case localException => {
	       localException.printStackTrace();
	       return null
	     }
	   }
	}
}