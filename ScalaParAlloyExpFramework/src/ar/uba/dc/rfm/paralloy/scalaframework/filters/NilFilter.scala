package ar.uba.dc.rfm.paralloy.scalaframework.filters

class NilFilter extends AbstractFilter {

  def clausesToKeep() = { (m => Nil) }

}