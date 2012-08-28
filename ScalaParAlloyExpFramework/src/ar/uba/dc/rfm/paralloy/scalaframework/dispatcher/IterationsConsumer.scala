package ar.uba.dc.rfm.paralloy.scalaframework.dispatcher

import scala.actors.Actor
import ar.uba.dc.rfm.paralloy.scalaframework.Iteration
import scala.collection.mutable.Queue
import ar.uba.dc.rfm.paralloy.scalaframework.IterationSolverActor
import ar.uba.dc.rfm.paralloy.scalaframework.SolveThisMessage

case class NewIterationMessage(sender : IterationsQueue)
case class TakeIterationMessage(it : Iteration)
case class IterationFinishedMessage(sender : IterationSolverActor, r : (Char, Double))

class IterationsConsumer() extends Actor {
	var free = new Queue[IterationSolverActor]
	var waiting = new Queue[Iteration]
	var toBeConsumed = 0
	var queueActor : Actor = null
	var starts = 0
	var ends = 0
	
	def this(concurrency : Int) {
		this()
		
		for(i <- List.range(0, concurrency)){
		  object it extends IterationSolverActor
		  it.start()
		  free.enqueue(it)
		}
	}
	
	def act() {
	  loop {
	    react {
	      case NewIterationMessage(sender) => {
	        if(free.size > 0)
	        	sender ! ConsumeIterationMessage(this)
	        else {
	          queueActor = sender
	          toBeConsumed += 1
	        }
	      }
	      case TakeIterationMessage(it : Iteration) => {
	        if(free.size > 0) {
		        var actor = free.dequeue()
		        actor ! SolveThisMessage(this, it)
		        starts += 1
	        }
	        else
	          waiting.enqueue(it)
	        
	      }
	      case IterationFinishedMessage(s, r) => {
	        ends += 1
	        if(waiting.size <= 0)
	        {
	        	free.enqueue(s)
	        	if(toBeConsumed > 0) {
	        	  toBeConsumed -= 1
	        	  queueActor ! ConsumeIterationMessage(this)
	        	}
	        }	  
	        else {
	          s ! SolveThisMessage(this, waiting.dequeue())
	          starts += 1
	        }
	      }
	      }
	  }
	}
}
