package ar.uba.dc.rfm.paralloy.scalaframework

import ar.uba.dc.rfm.paralloy.scalaframework.parsers.DIMACSParser
import scala.io.Source
import scala.collection.mutable.MutableList
import scala.util.control.Breaks._
import ar.uba.dc.rfm.minisat.intseq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.IntSeq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.Clause
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause
import ar.uba.dc.rfm.paralloy.scalaframework.filters.AbstractFilter
import ar.uba.dc.rfm.paralloy.scalaframework.lifters.AbstractLifter
import ar.uba.dc.rfm.paralloy.scalaframework.loggers.ExperimentLogger
import ar.uba.dc.rfm.paralloy.scalaframework.dispatcher.IterationsQueue
import ar.uba.dc.rfm.paralloy.scalaframework.dispatcher.ProduceIterationMessage
import scala.actors.Actor

import ar.uba.dc.rfm.paralloy.scalaframework.dispatcher.IterationFinishedMessage


case class SolvingBudget(propagationsBudget: Int, conflictsBudget: Int, timeBudget: Double)
case class ExperimentDefinition(itQueueActor: IterationsQueue, problems: List[String], iterations: Int, budget: SolvingBudget, lifter: AbstractLifter, filter: AbstractFilter, logger: ExperimentLogger)
case class Iteration(
    definition: ExperimentDefinition, 
    expId: Int, 
    pId: Int, 
    level: Int = 0, 
    its: Int, 
    path: String, 
    withLearnts: List[LearntClause], 
    assumedByParent: List[Int], 
    forMeToAssume: List[Int],
    maxLearnts : Int,
    currRestarts : Int)


case class EnqueProblemsMessage()
case class ExperimentFinishedMessage(id: Int, r: Char, t: Double)
class Experiment(definition: ExperimentDefinition) { //extends Actor {

  def run() {

    //this.start()
    definition.problems.map(runProblem)
  }

  def runProblem(path: String) {
    // Insert new experiment and retrieve id
    val id = definition.logger.newExperiment(
      path,
      definition.iterations,
      definition.budget.propagationsBudget,
      definition.budget.conflictsBudget,
      definition.budget.timeBudget,
      definition.lifter.getCannonicalAndParameterizedName,
      definition.filter.getCannonicalAndParameterizedName)

    definition.itQueueActor ! ProduceIterationMessage(Iteration(definition, id, -1, 0, definition.iterations, path, Nil, Nil, Nil, -1, -1))
  }

//  def act() {
//    loop {
//      react {
//        case EnqueProblemsMessage => run()
//        case ExperimentFinishedMessage(id, r, t) => { definition.logger.updateExpTime(id, r, t) }
//      }
//    }
//  }

}


case class SolveThisMessage(sender : Actor, i: Iteration)
case class InterruptSolvingMessage()

class IterationSolverActor extends Actor {

  case class InternalSolveThisMessage(parent : Actor, sender : Actor, i: Iteration)
  case class InternalIterationFinishedMessage(origSender : Actor, r : (Char, Double))
  
  var interrupted: Boolean = false
  
  InternalActor.start()
  
  object InternalActor extends Actor {
    var iteration : Iteration = null
    
    def signPermutations(vars: List[Int]): List[List[Int]] = {
      def f(l: List[Int]): List[List[Int]] = {
        l match {
          case Nil => throw new Exception("Ooops, tratando de generar las permutaciones de signos sobre una lista vacía")
          case x :: Nil => (x :: Nil) :: (x * -1 :: Nil) :: Nil
          case x :: xs => f(xs).map(x :: _) ::: f(xs).map((-1 * x) :: _)
        }
      }
      f(vars.map(_.abs))
    }
  
    def iterate(): (Char, Double) = {
      val assuming = iteration.assumedByParent ::: iteration.forMeToAssume
      Console.printf("Iterations: %d, assuming: [%s], with this many learnts: %d\n", iteration.its, assuming.mkString(" "), iteration.withLearnts.size)
      Console.flush

      var s = new Minisat(iteration.path)
      s.setVerbosity(1)
      val SolvingBudget(p, c, t) = iteration.definition.budget

      // Save this iteration
      val id = iteration.definition.logger.newIteration(iteration.expId, iteration.pId, iteration.its, iteration.level, iteration.forMeToAssume)
      s.prepare_for_solving(Nil, iteration.withLearnts, assuming)

      val (res, elapsed) =
        if (s.simplify()) {
          // If didn't die by propagation
          if (iteration.its <= 1) {
            // Discard budget and solve to infinity and beyond...
            var res = 'I'
            var elapsed = 0d
            var learnts = iteration.maxLearnts
            var restarts = iteration.currRestarts
            
            // Solve until finished or until interrupted
            while (res == 'I' && !interrupted) {
              res = s.solve_time_restricted(30d, learnts, restarts)
              elapsed += s.get_last_execution_time.toDouble / 1000
              learnts = s.get_max_learnts()
              restarts = s.get_current_restarts()
            }
            if (interrupted) Console println "Solving interrupted"

            // Best effort to free memory used by Minisat
            // once we don't need it anymore
            s.finalize()
            s = null
            System.gc()

            (res, elapsed)
          } else {
            val res = s.solve_restricted(
                t * scala.math.pow(1.5, iteration.level), 
                c, 
                p, 
                iteration.maxLearnts, 
                iteration.currRestarts)
            Console flush
            val elapsed = s.get_last_execution_time.toDouble / 1000
            if (res == 'I') {
              // Variables to lift sorted by value

              val toLift = iteration.definition.lifter.variablesToLift(iteration.level)(s).sortWith((a: Int, b: Int) ⇒ a.abs < b.abs)
              val learnts = iteration.definition.filter.clausesToKeep()(s)

              var max_learnts = s.get_max_learnts()
              var restarts = s.get_current_restarts()
              
              // Best effort to free memory used by Minisat
              // once we don't need it anymore
              s.finalize()
              s = null
              System.gc()

              // Try to compute in parallel
              val lifting = signPermutations(toLift)
              lifting.foreach((as: List[Int]) => {
                iteration.definition.itQueueActor ! ProduceIterationMessage(
                    Iteration(
                        iteration.definition, 
                        iteration.expId, 
                        id, 
                        iteration.level + 1, 
                        iteration.its - 1, 
                        iteration.path, 
                        learnts, 
                        assuming, 
                        as,
                        max_learnts,
                        restarts
                    ))
              })

              // Aggregate results
              // foldLeft should be correct even in parallel collections 
              //            val (rr, tr) = partial_res.foldLeft(('U', 0.0))((a, b) ⇒ {
              //              val (ra, ta) = a
              //              val (rb, tb) = b
              //              (if (ra == 'S') 'S' else if (rb == 'B') ra else rb, ta + tb)
              //            })

              ('I', elapsed)
            } else
              (res, elapsed)
          }
        } else {
          // If killed by propagation
          ('B', 0.0) // 'B' means UNSAT killed by pure propagation
        }

      iteration.definition.logger.updateItTime(id, res, elapsed)

      Console.printf("-- Ended solving it: %d with res: %c and time: %fs\n", id, res, elapsed)
      Console.flush
      
      // Try to free some more memory
      iteration = null
      System.gc()
      
      (res, elapsed)
    }
    
    
    
    def act() {
      loop {
        react {
          case InternalSolveThisMessage(parent, sender, i) => {
            iteration = i
            val res = iterate()
            parent ! InternalIterationFinishedMessage(sender, res)
          }
        }
      }
    }
  }

  def act() {
    loop {
      react {
        case SolveThisMessage(sender, i) => InternalActor ! InternalSolveThisMessage(this, sender, i)
        case InterruptSolvingMessage => interrupted = true
        case InternalIterationFinishedMessage(s, r) => s ! IterationFinishedMessage(this, r)
      }
    }
  }
  
  def iteration() = InternalActor.iteration
}
