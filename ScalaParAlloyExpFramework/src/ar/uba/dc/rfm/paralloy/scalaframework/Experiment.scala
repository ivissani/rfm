package ar.uba.dc.rfm.paralloy.scalaframework

import ar.uba.dc.rfm.paralloy.scalaframework.parsers.DIMACSParser
import scala.io.Source
import scala.collection.mutable.MutableList
import ar.uba.dc.rfm.minisat.intseq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.IntSeq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.Clause
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause
import ar.uba.dc.rfm.paralloy.scalaframework.filters.AbstractFilter
import scala.util.control.Breaks._

case class SolvingBudget(propagationsBudget: Int, conflictsBudget: Int, timeBudget: Double)

class Experiment(
  problems: List[String],
  iterations: Int,
  budget: SolvingBudget,
  lifter: AbstractLifter,
  filter: AbstractFilter) {
  def run() {
    val totalTimes = problems.map(runProblem(_))
  }

  def runProblem(path: String): (Char, Double) = {
//    val solver = new Minisat
//    solver.read(path)
//    val what = solver.get_clauses
//    iterate(iterations, what, Nil, Nil)
    iterate(iterations, path, Nil, Nil)
  }

  def signPermutations(vars: List[Int]): List[List[Int]] = {
    def f(l: List[Int], factor: Int): List[List[Int]] = {
      var res: List[List[Int]] = Nil
      for (i <- List.range(0, l.length + 1))
        for (l2 <- l.combinations(i))
          res = l2.map(factor * _) :: res
      res
    }

    def g(l: List[Int]): List[List[Int]] = {
      f(l, -1).zip(f(l, 1).reverse).map(p => p._1 ::: p._2)
    }

    g(vars.map(_.abs))
  }

  def iterate(its: Int, path: String, withLearnts: List[LearntClause], assuming: List[Int]): (Char, Double) = {
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
    } else {

      res = s.solve_restricted(Nil, t, c, p, withLearnts, assuming)
      elapsed = s.get_last_execution_time.toDouble / 1000
      if (res == 'I') {
        val toLift = lifter.variablesToLift()(s)
        val learnts = filter.clausesToKeep()(s)

        res = 'U'
        breakable {
          signPermutations(toLift).foreach((as: List[Int]) => {
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