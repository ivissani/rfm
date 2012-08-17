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

// Import the session management, including the implicit threadLocalSession
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

// Import the query language
import org.scalaquery.ql._

// Import the standard SQL types
import org.scalaquery.ql.TypeMapper._

// Use H2Driver which implements ExtendedProfile and thus requires ExtendedTables
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ ExtendedTable ⇒ Table }

case class SolvingBudget(propagationsBudget : Int, conflictsBudget : Int, timeBudget : Double)

object ExperimentTable extends Table[(Int, String, Int, Int, Int, Double, String, String, String, Double)]("EXPERIMENTS") {
  def id = column[Int]("id", O AutoInc, O NotNull, O PrimaryKey)
  def cnf = column[String]("cnf", O NotNull, O DBType ("varchar(2048)"))
  def iterations = column[Int]("iterations", O NotNull)
  def propagationsBudget = column[Int]("prop_budg", O NotNull, O Default (-1))
  def conflictsBudget = column[Int]("conf_budg", O NotNull, O Default (-1))
  def timeBudget = column[Double]("time_budg", O NotNull, O Default (-1.0))
  def lifterClassName = column[String]("lifter", O NotNull, O DBType ("varchar(2048)"))
  def filterClassName = column[String]("filter", O NotNull, O DBType ("varchar(2048)"))
  def result = column[String]("result", O NotNull, O Default ("I"), O DBType ("char(1)"))
  def totalTime = column[Double]("time", O Nullable)

  def * = id ~ cnf ~ iterations ~ propagationsBudget ~ conflictsBudget ~ timeBudget ~ lifterClassName ~ filterClassName ~ result ~ totalTime
  def newExp = ExperimentTable.cnf ~ ExperimentTable.iterations ~ ExperimentTable.propagationsBudget ~ ExperimentTable.conflictsBudget ~ ExperimentTable.timeBudget ~ ExperimentTable.lifterClassName ~ ExperimentTable.filterClassName
}

class Experiment(
  problems : List[String],
  iterations : Int,
  budget : SolvingBudget,
  lifter : AbstractLifter,
  filter : AbstractFilter) {
  def run() {

    // Connect to the database and execute the following block within a session
    Database.forURL("jdbc:h2:~/scalloy.results", "sa", "", driver = "org.h2.Driver") withSession {
      val totalTimes = problems.map(runProblem(_))
    }
  }

  def runProblem(path : String) : (Char, Double) = {
    ExperimentTable.newExp.insert(path,
      iterations,
      budget.propagationsBudget,
      budget.conflictsBudget,
      budget.timeBudget,
      lifter.getClass().getCanonicalName(),
      filter.getClass().getCanonicalName())

    iterate(iterations, path, Nil, Nil)
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

  def iterate(its : Int, path : String, withLearnts : List[LearntClause], assuming : List[Int]) : (Char, Double) = {
    Console.printf("Iterations: %d, assuming: [%s]\n", its, assuming.mkString(" "))
    Console.flush

    val s = new Minisat
    s.read(path)
    val SolvingBudget(p, c, t) = budget
    var res = 'I'
    var elapsed = 0.0

    if (its <= 1) {
      // Discard budget and solve to infinity and beyond...
      res = s.solve_unrestricted(Nil, withLearnts, assuming)
      elapsed = s.get_last_execution_time.toDouble / 1000
    }
    else {

      res = s.solve_restricted(Nil, t, c, p, withLearnts, assuming)
      elapsed = s.get_last_execution_time.toDouble / 1000
      if (res == 'I') {
        val toLift = lifter.variablesToLift()(s)
        val learnts = filter.clausesToKeep()(s)

        res = 'U'
        breakable {
          signPermutations(toLift).foreach((as : List[Int]) ⇒ {
            val (r, t) = iterate(its - 1, path, learnts, assuming ::: as)
            elapsed += t
            if (r == 'S') { res = r; break }
          })
        }
      }
    }
    Console.printf("-- Ended solving with res: %c and time: %fs\n", res, elapsed)
    Console.flush
    (res, elapsed)
  }
}