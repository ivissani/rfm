package ar.uba.dc.rfm.paralloy.scalaframework.lifters

import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import scala.util.Random
import ar.uba.dc.rfm.minisat.intseq
import scala.collection.mutable.HashSet
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.IntSeq

class PseudoRandomLifter(seed : Long, limit : Int) extends AbstractLifter {
  def variablesToLift(level : Int) = { 
    def ret(m : Minisat) : List[Int] = {
      val vars = m.nVars()
      val random = new Random(seed)
      
      var s = new HashSet[Int]
      m.getLearntFacts.foreach(i => s.add(i.abs))
      
      random.
      	shuffle(List.range(1, vars+1)).
      		filter(!s.contains(_)).drop((level-1) * limit).
      			take(limit).
      				toList
    }
    
    ret
  }

  def getCannonicalAndParameterizedName(): String = {
    val res = getClass().getCanonicalName()
    res + "(seed=%d, limit=%d)".format(seed, limit)
  }

}