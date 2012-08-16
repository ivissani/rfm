import ar.uba.dc.rfm.paralloy.scalaframework.parsers.LearntsParser
import scala.io.Source
import ar.uba.dc.rfm.paralloy.scalaframework.Als2CnfActor
import scala.actors.Actor._
import java.io.File
import scala.actors.Actor
import ar.uba.dc.rfm.paralloy.scalaframework.Experiment
import ar.uba.dc.rfm.paralloy.scalaframework.SolvingBudget
import ar.uba.dc.rfm.paralloy.scalaframework.lifters.VarActivityLifter
import ar.uba.dc.rfm.paralloy.scalaframework.filters.NilFilter
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.Clause
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.Clause

object Main {

  def main(args: Array[String]): Unit = {
    //    object als2cnfactor extends Als2CnfActor
    //    als2cnfactor.start()
    //    
    //    val receiverActor = actor {
    //      loop {
    //        react {
    //          case translation : String => {
    //            Console println "-------------------------"
    //            Console println "Ahí me llegó la respuesta"
    //            Console println translation
    //          }
    //          case (alsfile : File) => {
    //        	  als2cnfactor ! Tuple2(alsfile, self)
    //        	  Console println "Ya volví"
    //        	  Console println "-------------------------"
    //          } 
    //        }
    //      }
    //    }
    //    
    //    receiverActor.start()
    //    
    //    receiverActor ! (new File("src/test/data/pamela9.als"))
    //  }
    System.loadLibrary("minisat")
    val ex = new Experiment("/home/ivissani/RFM/miscosas/minisat/cnf/p8.cnf" :: Nil, 3, SolvingBudget(-1, -1, 15d), new VarActivityLifter(2), new NilFilter)
    ex.run
  }
}