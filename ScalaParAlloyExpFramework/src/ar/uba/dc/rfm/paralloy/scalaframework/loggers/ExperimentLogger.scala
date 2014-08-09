package ar.uba.dc.rfm.paralloy.scalaframework.loggers

import java.sql.Timestamp

abstract class ExperimentLogger {
	def newIteration(expId : Int, pId : Int, its : Int, level : Int, assuming : List[Int]) : Int
	def newExperiment(path : String, iterations : Int, propagationsBudget : Int, conflictsBudget : Int, timeBudget : Double, lifterName : String, filterName : String, keepLearntsLimit : Boolean, keepRestarts : Boolean, keepLearntFacts : Boolean) : Int
	def updateExpTime(id : Int, result : Char, time : Double) : Timestamp
	def updateItTime(id : Int, result : Char, time : Double) : Timestamp
}