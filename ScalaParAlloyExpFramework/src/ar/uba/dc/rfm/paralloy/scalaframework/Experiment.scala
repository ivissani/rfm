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
case class Iteration(definition: ExperimentDefinition, expId: Int, pId: Int, level: Int = 0, its: Int, path: String, withLearnts: List[LearntClause], assumedByParent: List[Int], forMeToAssume: List[Int])

case class EnqueProblemsMessage
case class ExperimentFinishedMessage(id: Int, r: Char, t: Double)

class Experiment(definition: ExperimentDefinition) extends Actor {

  def run() {
    this.start()
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

    definition.itQueueActor ! ProduceIterationMessage(Iteration(definition, id, -1, 0, definition.iterations, path, Nil, Nil, Nil))
  }

  def act() {
    loop {
      react {
        case EnqueProblemsMessage => run()
        case ExperimentFinishedMessage(id, r, t) => { definition.logger.updateExpTime(id, r, t) }
      }
    }
  }

}

case class SolveThisMessage(sender : Actor, i: Iteration)
case class InterruptSolvingMessage

class IterationSolverActor extends Actor {
  case class InternalSolveThisMessage(parent : Actor, sender : Actor, i: Iteration)
  case class InternalIterationFinishedMessage(origSender : Actor, r : (Char, Double))
  
  var interrupted: Boolean = false
  
  InternalActor.start()
  
  object InternalActor extends Actor {
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

    def iterate(definition: ExperimentDefinition, expId: Int, pId: Int = -1, level: Int = 0, its: Int, path: String, withLearnts: List[LearntClause], assumedByParent: List[Int], forMeToAssume: List[Int]): (Char, Double) = {
      val assuming = assumedByParent ::: forMeToAssume
      Console.printf("Iterations: %d, assuming: [%s], with this many learnts: %d\n", its, assuming.mkString(" "), withLearnts.size)
      Console.flush

      var s = new Minisat(path)
      s.setVerbosity(1)
      val SolvingBudget(p, c, t) = definition.budget

      // Save this iteration
      val id = definition.logger.newIteration(expId, pId, its, level, forMeToAssume)
      s.prepare_for_solving(Nil, withLearnts, assuming)

      val (res, elapsed) =
        if (s.simplify()) {
          // If didn't die by propagation
          if (its <= 1) {
            // Discard budget and solve to infinity and beyond...
            var res = 'I'
            var elapsed = 0d
            
            // Solve until finished or until interrupted
            while (res == 'I' && !interrupted) {
              res = s.solve_time_restricted(30d)
              elapsed += s.get_last_execution_time.toDouble / 1000
            }
            if (interrupted) Console println "Solving interrupted"

            // Best effort to free memory used by Minisat
            // once we don't need it anymore
            s.finalize()
            s = null
            System.gc()

            (res, elapsed)
          } else {
            val res = s.solve_restricted(t, c, p)
            Console flush
            val elapsed = s.get_last_execution_time.toDouble / 1000
            if (res == 'I') {
              // Variables to lift sorted by value
              val toLift = definition.lifter.variablesToLift(level)(s).sort((a: Int, b: Int) ⇒ a.abs < b.abs)
              val learnts = definition.filter.clausesToKeep()(s)

              // Best effort to free memory used by Minisat
              // once we don't need it anymore
              s.finalize()
              s = null
              System.gc()

              // Try to compute in parallel
              val lifting = signPermutations(toLift)
              lifting.foreach((as: List[Int]) => {
                definition.itQueueActor ! ProduceIterationMessage(Iteration(definition, expId, id, level + 1, its - 1, path, learnts, assuming, as))
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

      definition.logger.updateItTime(id, res, elapsed)

      Console.printf("-- Ended solving it: %d with res: %c and time: %fs\n", id, res, elapsed)
      Console.flush
      (res, elapsed)
    }
    def act() {
      loop {
        react {
          case InternalSolveThisMessage(parent, sender, i) => {
            val res = iterate(i.definition, i.expId, i.pId, i.level, i.its, i.path, i.withLearnts, i.assumedByParent, i.forMeToAssume)
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
}
