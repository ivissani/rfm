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

object Main {
  System.loadLibrary("minisat")
  
  var itConsumer : IterationsConsumer = new IterationsConsumer(3)
  var itQueueActor : IterationsQueue = new IterationsQueue(itConsumer)
  
  itConsumer.start()
  itQueueActor.start()
  
  var logger = new H2ExperimentLogger(Database.forURL("jdbc:h2:~/scalloy.results;AUTO_SERVER=TRUE", "sa", "", driver = "org.h2.Driver"))
  var waiting = new Queue[Experiment]
  
  def times() = logger.times
  
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
      scheduleInmediately : Boolean) {
    assert(iterations > 0)
    val exp = new Experiment(new ExperimentDefinition(itQueueActor, cnfs, iterations, SolvingBudget(propagationsBudget, conflictsBudget, timeBudget), lifter, filter, logger))
    
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
  
  def main(args : Array[String]) : Unit = {
	Main.enqueueExperiment("/home/ivissani/RFM/miscosas/minisat/cnf/sat/sgen1-sat-160-100.cnf" :: Nil, 2, -1, -1, 30d, new VarsFromLearntClauseLifter(5), new PercentageActivityFilter(0.1f), false)
	Main.schedule()
//	
//    val cnfs = List("./p7.cnf",
//      "./p8.cnf",
//      "./p9.cnf",
//      "./k8.cnf",
//      "./k9.cnf",
//      "./k10.cnf")
//    
////    val cnfs = List("/home/ivissani/RFM/miscosas/minisat/cnf/p7.cnf",
////      "/home/ivissani/RFM/miscosas/minisat/cnf/p8.cnf",
////      "/home/ivissani/RFM/miscosas/minisat/cnf/p9.cnf",
////      "/home/ivissani/RFM/miscosas/minisat/cnf/k8.cnf",
////      "/home/ivissani/RFM/miscosas/minisat/cnf/k9.cnf",
////      "/home/ivissani/RFM/miscosas/minisat/cnf/k10.cnf")
//
//    val base = new Experiment(ExperimentDefinition(itQueueActor, cnfs, 1, SolvingBudget(-1, -1, 15d), new PseudoRandomLifter(0, 2), new NilFilter, logger))
//
//    val seeds = List(
//      -3869081752602756812L)
//      
//    val perchita = for (s ← seeds) yield List(
//      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new NilFilter, logger)),
//      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.01f), logger)),
//      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.05f), logger)),
//      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.1f), logger)),
//      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.15f), logger)),
//      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.01f, true), logger)),
//      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.05f, true), logger)),
//      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.1f, true), logger)),
//      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.15f, true), logger)),
//      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(1f), logger)))
//
//    // For each seed, for each criteria, run!
//    perchita.foreach(_.foreach(_.run))
  }
}
