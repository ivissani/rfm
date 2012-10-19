package ar.uba.dc.rfm.paralloy.scalaframework.lifters

import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.LearntClause
import scala.collection.mutable.PriorityQueue
import scala.collection.Map

class CustomLifter(vars : Map[Int, List[Int]]) extends AbstractLifter {

  def variablesToLift(level : Int) : (Minisat ⇒ List[Int]) = {
    def f(m : Minisat) : List[Int] = vars(level)
    
    f
  }

  def getCannonicalAndParameterizedName(): String = { 
     this.getClass().getCanonicalName() + "(vars = %s)".format(vars.toString.replace("Map", ""))
  }

}