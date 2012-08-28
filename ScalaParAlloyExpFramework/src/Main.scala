import ar.uba.dc.rfm.paralloy.scalaframework.parsers.LearntsParser
import scala.io.Source
import ar.uba.dc.rfm.paralloy.scalaframework.Als2CnfActor
import scala.actors.Actor._
import java.io.File
import scala.actors.Actor
import ar.uba.dc.rfm.paralloy.scalaframework.Experiment
import ar.uba.dc.rfm.paralloy.scalaframework.SolvingBudget
import ar.uba.dc.rfm.paralloy.scalaframework.lifters.PseudoRandomLifter
import ar.uba.dc.rfm.paralloy.scalaframework.filters.NilFilter
import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.Clause
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.Clause
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.meta.{ MTable }
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.H2Driver.Implicit._
import ar.uba.dc.rfm.paralloy.scalaframework.filters.PercentageActivityFilter
import ar.uba.dc.rfm.paralloy.scalaframework.loggers.H2ExperimentLogger
import ar.uba.dc.rfm.paralloy.scalaframework.ExperimentDefinition
import ar.uba.dc.rfm.paralloy.scalaframework.dispatcher.IterationsQueue
import ar.uba.dc.rfm.paralloy.scalaframework.dispatcher.IterationsConsumer

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
    //      val e = new Experiment("src/test/data/pamela9.cnf" :: Nil, 3, SolvingBudget(-1, -1, 30d), new VarActivityLifter(5), new PercentageActivityFilter(0.1f))
    //      e.run()
    //    }
    //    return
    var logger = new H2ExperimentLogger

    logger.initialize(Database.forURL("jdbc:h2:~/scalloy.results;AUTO_SERVER=TRUE", "sa", "", driver = "org.h2.Driver"))

//    val cnfs = List("./p7.cnf",
//      "./p8.cnf",
//      "./p9.cnf",
//      "./k8.cnf",
//      "./k9.cnf",
//      "./k10.cnf")
    
    val cnfs = List("/home/ivissani/RFM/miscosas/minisat/cnf/p7.cnf",
      "/home/ivissani/RFM/miscosas/minisat/cnf/p8.cnf",
      "/home/ivissani/RFM/miscosas/minisat/cnf/p9.cnf",
      "/home/ivissani/RFM/miscosas/minisat/cnf/k8.cnf",
      "/home/ivissani/RFM/miscosas/minisat/cnf/k9.cnf",
      "/home/ivissani/RFM/miscosas/minisat/cnf/k10.cnf")

    object itConsumer extends IterationsConsumer(4)
    object itQueueActor extends IterationsQueue(itConsumer)
    val base = new Experiment(ExperimentDefinition(itQueueActor, cnfs, 1, SolvingBudget(-1, -1, 15d), new PseudoRandomLifter(0, 2), new NilFilter, logger))

    // 10 seeds generated using pseudo random generator
    val seeds = List(
      -3869081752602756812L,
      -6519113038106920900L,
      4541925429225719055L,
      2371210991396154438L,
      6672958499860008106L,
      -3849111578446398228L,
      4446533789556373787L,
      -8277698858615201367L,
      1025911294265486391L,
      -442510427907438972L)

    val perchita = for (s ← seeds) yield List(
      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new NilFilter, logger)),
      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.01f), logger)),
      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.05f), logger)),
      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.1f), logger)),
      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.15f), logger)),
      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.01f, true), logger)),
      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.05f, true), logger)),
      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.1f, true), logger)),
      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(0.15f, true), logger)),
      new Experiment(ExperimentDefinition(itQueueActor, cnfs, 2, SolvingBudget(-1, -1, 30d), new PseudoRandomLifter(s, 5), new PercentageActivityFilter(1f), logger)))

    itConsumer.start()
    itQueueActor.start()

    // For each seed, for each criteria, run!
    perchita.foreach(_.foreach(_.run))
  }
}
