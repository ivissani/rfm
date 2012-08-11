package ar.uba.dc.rfm.paralloy.scalaframework.datatypes

case class DIMACS(variablesamount : Int, clausesamount : Int, clauses : List[Clause]) {
  def countVariables = {
    var maxVar = 0
    clauses foreach (c => c.literals foreach (lit => if (lit.abs > maxVar) maxVar = lit.abs))
    
    maxVar
  }
}