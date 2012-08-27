package ar.uba.dc.rfm.paralloy.scalaframework.dispatcher

import scala.collection.mutable.MutableList
import ar.uba.dc.rfm.paralloy.scalaframework.Iteration
import scala.actors.Actor
import scala.collection.mutable.Queue

case class ProduceIterationMessage(it : Iteration)
case class ConsumeIterationMessage(sender : IterationsConsumer)

class IterationsQueue(consumer : IterationsConsumer) extends Actor {
	var q = new Queue[Iteration]
	
	def act() {
	  loop {
	    react {
	      case ProduceIterationMessage(i) => { q.enqueue(i) ; consumer ! NewIterationMessage(this)}
	      case ConsumeIterationMessage(sender) => sender ! TakeIterationMessage(q.dequeue())
	    }
	  }
	}
}