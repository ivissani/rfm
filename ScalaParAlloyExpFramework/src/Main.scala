import ar.uba.dc.rfm.paralloy.scalaframework.parsers.LearntsParser
import scala.io.Source
import ar.uba.dc.rfm.paralloy.scalaframework.Als2CnfActor
import scala.actors.Actor._
import java.io.File
import scala.actors.Actor
import ar.uba.dc.rfm.paralloy.scalaframework.Experiment
import ar.uba.dc.rfm.paralloy.scalaframework.SolvingBudget
import ar.uba.dc.rfm.paralloy.scalaframework.lifters.PseudoRandomLifter
import ar.uba.dc.rfm.paralloy.scalaframework.filters.NilFilter
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.Clause
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.Clause
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.meta.{ MTable }
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.H2Driver.Implicit._
import ar.uba.dc.rfm.paralloy.scalaframework.filters.PercentageActivityFilter
import ar.uba.dc.rfm.paralloy.scalaframework.loggers.H2ExperimentLogger
import ar.uba.dc.rfm.paralloy.scalaframework.ExperimentDefinition
import ar.uba.dc.rfm.paralloy.scalaframework.dispatcher.IterationsQueue
import ar.uba.dc.rfm.paralloy.scalaframework.dispatcher.IterationsConsumer
import ar.uba.dc.rfm.paralloy.scalaframework.lifters.AbstractLifter
import ar.uba.dc.rfm.paralloy.scalaframework.filters.AbstractFilter
import ar.uba.dc.rfm.paralloy.scalaframework.ExperimentDefinition
import ar.uba.dc.rfm.paralloy.scalaframework.SolvingBudget
import scala.collection.mutable.Queue
import ar.uba.dc.rfm.paralloy.scalaframework.lifters.VarsFromLearntClauseLifter
import ar.uba.dc.rfm.paralloy.scalaframework.filters.PercentageActivityFilter
import ar.uba.dc.rfm.paralloy.scalaframework.lifters.VarsFromLearntClauseLifter
import ar.uba.dc.rfm.paralloy.scalaframework.filters.LengthFilter
import ar.uba.dc.rfm.paralloy.scalaframework.filters.LBDFilter
import ar.uba.dc.rfm.paralloy.scalaframework.filters.NilFilter
import ar.uba.dc.rfm.paralloy.scalaframework.dispatcher.IterationsConsumer

object Main {
  System.loadLibrary("minisat")
  
  var itConsumer : IterationsConsumer = new IterationsConsumer(3)
  var itQueueActor : IterationsQueue = new IterationsQueue(itConsumer)
  
  itConsumer.start()
  itQueueActor.start()
  
  var logger = new H2ExperimentLogger(Database.forURL("jdbc:h2:~/scalloy.results;AUTO_SERVER=TRUE", "sa", "", driver = "org.h2.Driver"))
  var waiting = new Queue[Experiment]
  
  def times() = logger.times
  
  def setWorkers(amount : Int) {
    itConsumer = new IterationsConsumer(amount)
    itQueueActor = new IterationsQueue(itConsumer)
    
    itConsumer.start()
    itQueueActor.start()
  }
  
  // 10 seeds generated using pseudo random generator
  def seeds = for(s <- List(
      -3869081752602756812L,
      -6519113038106920900L,
      4541925429225719055L,
      2371210991396154438L,
      6672958499860008106L,
      -3849111578446398228L,
      4446533789556373787L,
      -8277698858615201367L,
      1025911294265486391L,
      -442510427907438972L)) yield s
  
  def enqueueExperiment[T <: AbstractLifter, U <: AbstractFilter](
      cnfs : List[String], 
      iterations : Int, 
      conflictsBudget : Int, 
      propagationsBudget : Int, 
      timeBudget : Double, 
      lifter : T, 
      filter : U,
      keepLearntsLimit : Boolean,
      keepRestarts : Boolean,
      keepLearntFacts : Boolean,
      scheduleInmediately : Boolean) {
    assert(iterations > 0)
    val exp = new Experiment(
        new ExperimentDefinition(
            itQueueActor, 
            cnfs, 
            iterations, 
            SolvingBudget(propagationsBudget, conflictsBudget, timeBudget), 
            lifter, 
            filter, 
            logger,
            keepLearntsLimit,
            keepRestarts,
            keepLearntFacts))
    
    if(scheduleInmediately)
      exp.run
    else
      waiting.enqueue(exp)
  }
  
  def schedule() {
    while(waiting.size > 0) 
    {
      waiting.dequeue.run
    }
  }
  
  def benchmark(
      cnf : String, 
      keepLearntsLimit : Boolean = false, 
      keepRestarts : Boolean = false, 
      keepLearntFacts : Boolean = true, 
      keepLearntFactsAppliesToNullCriteria : Boolean = true) {
    for(s <- Main.seeds) {
	  for(i <- List.range(2, 7)) {
	    Main.enqueueExperiment(cnf::Nil, 2, -1, -1, 60d, new PseudoRandomLifter(s, 5), new LengthFilter(i), keepLearntsLimit, keepRestarts, keepLearntFacts, false)
	    Main.enqueueExperiment(cnf::Nil, 2, -1, -1, 60d, new PseudoRandomLifter(s, 5), new LBDFilter(i), keepLearntsLimit, keepRestarts, keepLearntFacts, false)
	  }
	  for(p <- List(0.05f, 0.1f, 0.15f, 0.2f)) {
	    for(keep <- List(true, false)) {
	      for(less <- List(true, false)) {
	    	  Main.enqueueExperiment(cnf::Nil, 2, -1, -1, 60d, new PseudoRandomLifter(s, 5), new PercentageActivityFilter(p, less, keep), keepLearntsLimit, keepRestarts, keepLearntFacts, false)
	      }
	    }
	  }
	  Main.enqueueExperiment(cnf::Nil, 2, -1, -1, 60d, new PseudoRandomLifter(s, 5), new PercentageActivityFilter(1f), keepLearntsLimit, keepRestarts, keepLearntFacts, false)
	  Main.enqueueExperiment(cnf::Nil, 2, -1, -1, 60d, new PseudoRandomLifter(s, 5), new NilFilter, keepLearntsLimit, keepRestarts, keepLearntFactsAppliesToNullCriteria && keepLearntFacts, false)	    
	}
  }
  
  def main(args : Array[String]) : Unit = {
	//Main.enqueueExperiment("/home/ivissani/RFM/miscosas/minisat/cnf/sat/sgen1-sat-160-100.cnf" :: Nil, 5, -1, -1, 15d, new VarsFromLearntClauseLifter(2), new PercentageActivityFilter(0.1f), false, false, true, false)
    Main.enqueueExperiment("/home/ivissani/git/rfm/tesis/benchmark/cnf/k10.cnf" :: Nil, 2, -1, -1, 30d, new VarsFromLearntClauseLifter(5), new PercentageActivityFilter(0.1f), false, false, true, false)
	Main.schedule()
  }
}
