package ar.uba.dc.rfm.paralloy.scalaframework.lifters
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat

abstract class AbstractLifter {
  def variablesToLift() : (Minisat ⇒ List[Int])
  def getCannonicalAndParameterizedName() : String
}