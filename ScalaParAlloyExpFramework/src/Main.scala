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
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.H2Driver.Implicit._
import ar.uba.dc.rfm.paralloy.scalaframework.filters.PercentageActivityFilter
import ar.uba.dc.rfm.paralloy.scalaframework.loggers.H2ExperimentLogger

object Main {

  def main(args : Array[String]) : Unit = {
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

//    Database.forURL("jdbc:h2:~/scalloy.results;AUTO_SERVER=TRUE", "sa", "", driver = "org.h2.Driver") withSession {
//      def makeTableMap(implicit dbsess : Session) : Map[String, MTable] = {
//        val tableList = MTable.getTables.list()(dbsess);
//        val tableMap = tableList.map { t ⇒ (t.name.name, t) }.toMap;
//        tableMap;
//      }
//      // Connect to the database and execute the following block within a session
//
//      val tables = makeTableMap
//      if (!tables.contains(Experiments.tableName)) { Experiments.ddl create }
//      if (!tables.contains(Iterations.tableName)) { Iterations.ddl create }
//      if (!tables.contains(AssumedLiterals.tableName)) { AssumedLiterals.ddl create }
//      val e = new Experiment("src/test/data/pamela9.cnf" :: Nil, 3, SolvingBudget(-1, -1, 30d), new VarActivityLifter(4), new PercentageActivityFilter(0.1f))
//      e.run()
//    }
//    return
    var logger = new H2ExperimentLogger
    Database.forURL("jdbc:h2:~/scalloy.results;AUTO_SERVER=TRUE", "sa", "", driver = "org.h2.Driver") withSession {
      
      ses : Session  => logger.initialize(ses)
    }
    val cnfs = List("./p7.cnf",
      "./p8.cnf",
      "./p9.cnf",
      "./k8.cnf",
      "./k9.cnf",
      "./k10.cnf")
    
    val base = new Experiment(cnfs, 1, SolvingBudget(-1, -1, 15d), new VarActivityLifter(2), new NilFilter, logger)

    val perchita = List(
//      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 15d), new VarActivityLifter(2), new NilFilter),
//      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 30d), new VarActivityLifter(2), new NilFilter),
//      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 60d), new VarActivityLifter(2), new NilFilter),
//      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 120d), new VarActivityLifter(2), new NilFilter),
//      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 15d), new VarActivityLifter(3), new NilFilter),
//      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 30d), new VarActivityLifter(3), new NilFilter),
//      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 60d), new VarActivityLifter(3), new NilFilter),
//      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 120d), new VarActivityLifter(3), new NilFilter),
      //new Experiment(cnfs, 2, SolvingBudget(-1, -1, 15d), new VarActivityLifter(4), new NilFilter),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 30d), new VarActivityLifter(4), new NilFilter, logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 60d), new VarActivityLifter(4), new NilFilter, logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 120d), new VarActivityLifter(4), new NilFilter, logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 30d), new VarActivityLifter(4), new PercentageActivityFilter(0.05f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 60d), new VarActivityLifter(4), new PercentageActivityFilter(0.05f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 120d), new VarActivityLifter(4), new PercentageActivityFilter(0.05f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 30d), new VarActivityLifter(4), new PercentageActivityFilter(0.1f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 60d), new VarActivityLifter(4), new PercentageActivityFilter(0.1f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 120d), new VarActivityLifter(4), new PercentageActivityFilter(0.1f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 30d), new VarActivityLifter(4), new PercentageActivityFilter(0.15f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 60d), new VarActivityLifter(4), new PercentageActivityFilter(0.15f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 120d), new VarActivityLifter(4), new PercentageActivityFilter(0.15f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 30d), new VarActivityLifter(4), new PercentageActivityFilter(0.05f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 60d), new VarActivityLifter(4), new PercentageActivityFilter(0.05f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 120d), new VarActivityLifter(4), new PercentageActivityFilter(0.05f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 30d), new VarActivityLifter(4), new PercentageActivityFilter(0.1f, true), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 60d), new VarActivityLifter(4), new PercentageActivityFilter(0.1f, true), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 120d), new VarActivityLifter(4), new PercentageActivityFilter(0.1f, true), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 30d), new VarActivityLifter(4), new PercentageActivityFilter(0.15f, true), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 60d), new VarActivityLifter(4), new PercentageActivityFilter(0.15f, true), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 120d), new VarActivityLifter(4), new PercentageActivityFilter(0.15f, true), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 30d), new VarActivityLifter(4), new PercentageActivityFilter(1f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 60d), new VarActivityLifter(4), new PercentageActivityFilter(1f), logger),
      new Experiment(cnfs, 2, SolvingBudget(-1, -1, 120d), new VarActivityLifter(4), new PercentageActivityFilter(1f), logger))

//    val exps = List(
//      new Experiment(cnfs, 3, SolvingBudget(-1, -1, 15d), new VarActivityLifter(2), new NilFilter),
//      new Experiment(cnfs, 3, SolvingBudget(-1, -1, 30d), new VarActivityLifter(2), new NilFilter),
//      new Experiment(cnfs, 3, SolvingBudget(-1, -1, 60d), new VarActivityLifter(2), new NilFilter),
//      new Experiment(cnfs, 3, SolvingBudget(-1, -1, 120d), new VarActivityLifter(2), new NilFilter),
//      new Experiment(cnfs, 3, SolvingBudget(-1, -1, 15d), new VarActivityLifter(3), new NilFilter),
//      new Experiment(cnfs, 3, SolvingBudget(-1, -1, 30d), new VarActivityLifter(3), new NilFilter),
//      new Experiment(cnfs, 3, SolvingBudget(-1, -1, 60d), new VarActivityLifter(3), new NilFilter),
//      new Experiment(cnfs, 3, SolvingBudget(-1, -1, 120d), new VarActivityLifter(3), new NilFilter),
//      new Experiment(cnfs, 3, SolvingBudget(-1, -1, 15d), new VarActivityLifter(4), new NilFilter),
//      new Experiment(cnfs, 3, SolvingBudget(-1, -1, 30d), new VarActivityLifter(4), new NilFilter),
//      new Experiment(cnfs, 3, SolvingBudget(-1, -1, 60d), new VarActivityLifter(4), new NilFilter),
//      new Experiment(cnfs, 3, SolvingBudget(-1, -1, 120d), new VarActivityLifter(4), new NilFilter))
      
      base.run()
      perchita.foreach(_.run)
  }
}