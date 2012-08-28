package ar.uba.dc.rfm.paralloy.scalaframework.lifters

import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import scala.util.Random

class PseudoRandomLifter(seed : Long, limit : Int) extends AbstractLifter {
  def variablesToLift(level : Int) = { 
    def ret(m : Minisat) : List[Int] = {
      val vars = m.nVars()
      val random = new Random(seed)
      
      random.shuffle(List.range(1, vars+1)).drop((level-1) * limit).take(limit).toList
    }
    
    ret
  }

  def getCannonicalAndParameterizedName(): String = {
    val res = getClass().getCanonicalName()
    res + "(seed=%d, limit=%d)".format(seed, limit)
  }

}