package ar.uba.dc.rfm.paralloy.scalaframework.dispatcher

import scala.collection.mutable.MutableList
import ar.uba.dc.rfm.paralloy.scalaframework.Iteration
import scala.actors.Actor
import scala.collection.mutable.Queue
import scala.collection.mutable.Stack

case class ProduceIterationMessage(it : Iteration)
case class ConsumeIterationMessage(sender : IterationsConsumer)

class IterationsQueue(consumer : IterationsConsumer) extends Actor {
	var q = new Stack[Iteration]
	
	def act() {
	  loop {
	    react {
	      case ProduceIterationMessage(i) => { 
	        q.push(i)
	        consumer ! NewIterationMessage(this)
	        Console println "#*#*#*#*#* Nueva iteración"
	      }
	      case ConsumeIterationMessage(sender) => {
	        sender ! TakeIterationMessage(q.pop())
	        Console println "#*#*#*#*#* Iteración consumida"
	      }
	    }
	  }
	}
}