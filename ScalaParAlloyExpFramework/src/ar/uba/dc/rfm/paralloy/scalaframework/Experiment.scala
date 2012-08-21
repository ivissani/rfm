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
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ ExtendedTable ⇒ Table }

import java.sql.Date
import java.net.InetAddress
import java.sql.Timestamp

case class SolvingBudget(propagationsBudget : Int, conflictsBudget : Int, timeBudget : Double)

object Experiments extends Table[(Int, String, Int, Int, Int, Double, String, String, String, Option[Double], String, Timestamp)]("EXPERIMENTS") {
  def id = column[Int]("id", O AutoInc, O NotNull, O PrimaryKey)
  def cnf = column[String]("cnf", O NotNull, O DBType ("varchar(2048)"))
  def iterations = column[Int]("iterations", O NotNull)
  def propagationsBudget = column[Int]("prop_budg", O NotNull, O Default (-1))
  def conflictsBudget = column[Int]("conf_budg", O NotNull, O Default (-1))
  def timeBudget = column[Double]("time_budg", O NotNull, O Default (-1.0))
  def lifterClassName = column[String]("lifter", O NotNull, O DBType ("varchar(2048)"))
  def filterClassName = column[String]("filter", O NotNull, O DBType ("varchar(2048)"))
  def result = column[String]("result", O NotNull, O Default ("I"), O DBType ("char(1)"))
  def totalTime = column[Option[Double]]("time", O Nullable)
  def host = column[String]("host", O NotNull)
  def start = column[Timestamp]("start", O NotNull)

  def * = id ~ cnf ~ iterations ~ propagationsBudget ~ conflictsBudget ~ timeBudget ~ lifterClassName ~ filterClassName ~ result ~ totalTime ~ host ~ start
  def newExp = cnf ~ iterations ~ propagationsBudget ~ conflictsBudget ~ timeBudget ~ lifterClassName ~ filterClassName ~ host ~ start
}

object Iterations extends Table[(Int, Int, Option[Int], Int, Int, String, Option[Double], Timestamp)]("ITERATIONS") {
  def id = column[Int]("id", O AutoInc, O NotNull, O PrimaryKey)
  def experimentId = column[Int]("experiment_id", O NotNull)
  def parentIterationId = column[Option[Int]]("parent_id", O Nullable)
  def remainingIterations = column[Int]("remaining_iterations", O NotNull)
  def level = column[Int]("level", O NotNull)
  def result = column[String]("result", O NotNull, O Default ("I"), O DBType ("char(1)"))
  def totalTime = column[Option[Double]]("time", O Nullable)
  def start = column[Timestamp]("start", O NotNull)

  def * = id ~ experimentId ~ parentIterationId ~ remainingIterations ~ level ~ result ~ totalTime ~ start
  def newIt = experimentId ~ parentIterationId ~ remainingIterations ~ level ~ start
  def newRootIt = experimentId ~ remainingIterations ~ level ~ start

  // Foreign keys
  def fkExp = foreignKey("FK_EXPERIMENT", experimentId, Experiments)(_.id)
  def fkParent = foreignKey("FK_PARENT_ITERATION", parentIterationId, Iterations)(_.id.asInstanceOf[NamedColumn[Option[Int]]])
}

object AssumedLiterals extends Table[(Int, Int)]("ASSUMED_LITERALS") {
  def iterationId = column[Int]("iteration_id", O NotNull)
  def literal = column[Int]("literal", O NotNull)

  def * = iterationId ~ literal

  // Primary key
  def pk = primaryKey("PK", iterationId ~ literal)

  // Foreign keys
  def fkIteration = foreignKey("FK_ITERATION", iterationId, Iterations)(_.id)
}

class Experiment(
  problems : List[String],
  iterations : Int,
  budget : SolvingBudget,
  lifter : AbstractLifter,
  filter : AbstractFilter) {
  def run() {
    problems.par.map(runProblem(_))
  }

  private def newExperiment(path : String) : Int = {
    val ts = new Timestamp(System.currentTimeMillis())
    val host = InetAddress.getLocalHost().getHostName()
    Experiments.newExp.insert(path,
      iterations,
      budget.propagationsBudget,
      budget.conflictsBudget,
      budget.timeBudget,
      lifter.getCannonicalAndParameterizedName(),
      filter.getCannonicalAndParameterizedName(),
      host,
      ts)

    // Retrieve auto-generated id
    val q = for (e ← Experiments if e.cnf === path && e.host === host && e.start === ts) yield e.id
    // Should be only one
    if (q.list.length != 1) throw new Exception("Ooops, algo falló al insertar en la base de datos")
    q.first
  }

  def runProblem(path : String) : (Char, Double) = {
    // Insert new experiment and retrieve id
    val id = newExperiment(path)

    val (r, t) = iterate(expId = id, its = iterations, path = path, withLearnts = Nil, assumedByParent = Nil, forMeToAssume = Nil)

    // Update solving result and total elapsed time (in seconds)
    val uq = for (e ← Experiments if e.id === id) yield e.result ~ e.totalTime
    uq.update((r.toString, Option(t)))

    (r, t)
  }

  def signPermutations(vars : List[Int]) : List[List[Int]] = {
    def f(l : List[Int], factor : Int) : List[List[Int]] = {
      var res : List[List[Int]] = Nil
      for (i ← List.range(0, l.length + 1))
        for (l2 ← l.combinations(i))
          res = l2.map(factor * _) :: res
      res
    }

    def g(l : List[Int]) : List[List[Int]] = {
      f(l, -1).zip(f(l, 1).reverse).map(p ⇒ p._1 ::: p._2)
    }

    g(vars.map(_.abs))
  }

  private def newIteration(expId : Int, pId : Int, its : Int, level : Int, assuming : List[Int]) : Int = {
    val ts = new Timestamp(System.currentTimeMillis())
    val q =
      if (pId <= 0) {
        Iterations.newRootIt.insert(expId, its, level, ts)
        for (it ← Iterations if it.experimentId === expId && it.parentIterationId.isNull && it.remainingIterations === its && it.level === level && it.start === ts) yield it.id
      }
      else {
        Iterations.newIt.insert(expId, Option(pId), its, level, ts)
        for (it ← Iterations if it.experimentId === expId && it.parentIterationId === pId && it.remainingIterations === its && it.level === level && it.start === ts) yield it.id
      }
    if (q.list().length != 1) throw new Exception("Ooops, algo salió mal al recuperar el id de la iteración")
    val id = q.first

    // Save assumed literals
    // @TODO is this more efficient than "assuming.foreach(AssumedLiterals.*.insert(id, _))" ?
    AssumedLiterals.*.insertAll(assuming.map((id, _)) : _*)

    id
  }

  def iterate(expId : Int, pId : Int = -1, level : Int = 0, its : Int, path : String, withLearnts : List[LearntClause], assumedByParent : List[Int], forMeToAssume : List[Int]) : (Char, Double) = {
    val assuming = assumedByParent ::: forMeToAssume
    Console.printf("Iterations: %d, assuming: [%s]\n", its, assuming.mkString(" "))
    Console.flush

    val s = new Minisat
    s.read(path)
    s.setVerbosity(1)
    val SolvingBudget(p, c, t) = budget

    // Save this iteration
    val id = newIteration(expId, pId, its, level, forMeToAssume)

    val (res, elapsed) =
      if (its <= 1) {
        // Discard budget and solve to infinity and beyond...
        val res = s.solve_unrestricted(Nil, withLearnts, assuming)
        val elapsed = s.get_last_execution_time.toDouble / 1000
        (res, elapsed)
      }
      else {
        val res = s.solve_restricted(Nil, t, c, p, withLearnts, assuming)
        val elapsed = s.get_last_execution_time.toDouble / 1000
        if (res == 'I') {
          // Variables to lift sorted by value
          val toLift = lifter.variablesToLift()(s).sort((a : Int, b : Int) ⇒ a.abs < b.abs)
          val learnts = filter.clausesToKeep()(s)

          // Try to compute in parallel
          val partial_res = signPermutations(toLift).map((as : List[Int]) ⇒ {
            iterate(expId, id, level + 1, its - 1, path, learnts, assuming, as)
          })

          // Aggregate results
          // Fold is supposed to be correct even in parallel collections 
          val (rr, tr) = partial_res.fold(('U', 0.0))((a, b) ⇒ {
            val (ra, ta) = a
            val (rb, tb) = b
            (if (ra == 'S') 'S' else rb, ta + tb)
          })

          (rr, elapsed + tr)
        }
        else
          (res, elapsed)
      }

    // Update iteration with solving result and total elapsed time
    val qu = for (it ← Iterations if it.id === id) yield it.result ~ it.totalTime
    qu.update(res.toString, Option(elapsed))

    Console.printf("-- Ended solving with res: %c and time: %fs\n", res, elapsed)
    Console.flush
    (res, elapsed)
  }
}