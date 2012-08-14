package ar.uba.dc.rfm.paralloy.scalaframework

abstract class AbstractLifter {
  def variablesToLift() : (Minisat ⇒ List[Int])
}