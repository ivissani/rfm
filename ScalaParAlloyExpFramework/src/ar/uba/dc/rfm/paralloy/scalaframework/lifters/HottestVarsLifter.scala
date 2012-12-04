package ar.uba.dc.rfm.paralloy.scalaframework.lifters

import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause
import scala.collection.mutable.PriorityQueue

class HottestVarsLifter(limit : Int) extends AbstractLifter {

  def variablesToLift(level : Int) : (Minisat ⇒ List[Int]) = {
    def f(m : Minisat) : List[Int] = {
      implicit object ord extends Ordering[Tuple2[Int, Double]] {
        override def compare(x : Tuple2[Int, Double], y : Tuple2[Int, Double]) = {
          x._2.compare(y._2)
        }
      }
      var heap = new PriorityQueue[Tuple2[Int, Double]]
      val lf = m.getLearntFacts
      
      // Ordered enqueue every variable except for the ones that are already assigned 
      for(v <- List.range(1, m.nVars+1)) if(!lf.contains(v)) heap += Tuple2(v, m.get_var_activity(v))
      
      // Take hottest "limit" variables
      val ret = heap.take(limit).toList.map(_._1)
      
      // Try to free some memory
      heap = null
      System.gc()
      
      ret
    }
    
    f
  }

  def getCannonicalAndParameterizedName(): String = { 
     this.getClass().getCanonicalName() + "(limit = %d)".format(limit)
  }

}