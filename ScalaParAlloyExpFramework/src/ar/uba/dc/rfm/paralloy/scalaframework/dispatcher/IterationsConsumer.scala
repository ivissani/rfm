package ar.uba.dc.rfm.paralloy.scalaframework.dispatcher

import scala.actors.Actor
import ar.uba.dc.rfm.paralloy.scalaframework.Iteration

case class NewIterationMessage(sender : IterationsQueue)
case class TakeIterationMessage(it : Iteration)

class IterationsConsumer extends Actor {
	def act() {
	  loop {
	    react {
	      case NewIterationMessage(sender) => {sender ! ConsumeIterationMessage(this)}
	      case TakeIterationMessage(it : Iteration) => {}
	    }
	  }
	}
}