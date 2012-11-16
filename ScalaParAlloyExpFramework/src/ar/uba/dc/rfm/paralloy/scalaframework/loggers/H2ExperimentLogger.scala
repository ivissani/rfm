package ar.uba.dc.rfm.paralloy.scalaframework.loggers

import scala.collection.immutable.List
import java.sql.Date
import java.net.InetAddress
import java.sql.Timestamp
import java.net.InetAddress
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ ExtendedTable ⇒ Table }
import org.scalaquery.meta.{ MTable }
import scala.collection.parallel.mutable.ParSeq
import scala.collection.mutable.MutableList
import scala.collection.parallel.mutable.ParHashSet
import org.scalaquery.ql.extended.ExtendedColumnOption

object Experiments extends Table[(Int, String, Int, Int, Int, Double, String, String, String, Option[Double], String, Timestamp, Option[Timestamp], Boolean, Boolean, Boolean)]("EXPERIMENTS") {
  def id = column[Int]("id", O AutoInc, O NotNull, O PrimaryKey)
  def cnf = column[String]("cnf", O NotNull, O DBType ("varchar(2048)"))
  def iterations = column[Int]("iterations", O NotNull)
  def propagationsBudget = column[Int]("prop_budg", O NotNull, O Default (-1))
  def conflictsBudget = column[Int]("conf_budg", O NotNull, O Default (-1))
  def timeBudget = column[Double]("time_budg", O NotNull, O Default (-1.0))
  def lifterClassName = column[String]("lifter", O NotNull, O DBType ("varchar(2048)"))
  def filterClassName = column[String]("filter", O NotNull, O DBType ("varchar(2048)"))
  def result = column[String]("result", O NotNull, O Default ("I"), O DBType ("char(1)"))
  def totalTime = column[Option[Double]]("time", O Nullable)
  def host = column[String]("host", O NotNull)
  def start = column[Timestamp]("start", O NotNull)
  def end = column[Option[Timestamp]]("end", O Nullable)
  def keepLearntsLimit = column[Boolean]("keep_learnts_limit", O NotNull)
  def keepRestarts = column[Boolean]("keep_restarts", O NotNull)
  def keepLearntFacts = column[Boolean]("keep_learnt_facts", O NotNull)

  def * = id ~ cnf ~ iterations ~ propagationsBudget ~ conflictsBudget ~ timeBudget ~ lifterClassName ~ filterClassName ~ result ~ totalTime ~ host ~ start ~ end ~ keepLearntsLimit ~ keepRestarts ~ keepLearntFacts
  def newExp = cnf ~ iterations ~ propagationsBudget ~ conflictsBudget ~ timeBudget ~ lifterClassName ~ filterClassName ~ host ~ start ~ keepLearntsLimit ~ keepRestarts ~ keepLearntFacts
}

object Iterations extends Table[(Int, Int, Option[Int], Int, Int, String, Option[Double], Timestamp, Option[Timestamp])]("ITERATIONS") {
  def id = column[Int]("id", O AutoInc, O NotNull, O PrimaryKey)
  def experimentId = column[Int]("experiment_id", O NotNull)
  def parentIterationId = column[Option[Int]]("parent_id", O Nullable)
  def remainingIterations = column[Int]("remaining_iterations", O NotNull)
  def level = column[Int]("level", O NotNull)
  def result = column[String]("result", O NotNull, O Default ("I"), O DBType ("char(1)"))
  def totalTime = column[Option[Double]]("time", O Nullable)
  def start = column[Timestamp]("start", O NotNull)
  def end = column[Option[Timestamp]]("end", O Nullable)

  def * = id ~ experimentId ~ parentIterationId ~ remainingIterations ~ level ~ result ~ totalTime ~ start ~ end
  def newIt = experimentId ~ parentIterationId ~ remainingIterations ~ level ~ start
  def newRootIt = experimentId ~ remainingIterations ~ level ~ start

  // Foreign keys
  def fkExp = foreignKey("FK_EXPERIMENT", experimentId, Experiments)(_.id)
  def fkParent = foreignKey("FK_PARENT_ITERATION", parentIterationId, Iterations)(_.id.asInstanceOf[NamedColumn[Option[Int]]])
}

object AssumedLiterals extends Table[(Int, Int)]("ASSUMED_LITERALS") {
  def iterationId = column[Int]("iteration_id", O NotNull)
  def literal = column[Int]("literal", O NotNull)

  def * = iterationId ~ literal

  // Primary key
  def pk = primaryKey("PK", iterationId ~ literal)

  // Foreign keys
  def fkIteration = foreignKey("FK_ITERATION", iterationId, Iterations)(_.id)
}

class H2ExperimentLogger(var db : Database) extends ExperimentLogger {
  assert(db != null)

  initialize()

  private def initialize() {
    db withSession {
      def makeTableMap(implicit dbsess : Session) : Map[String, MTable] = {
        val tableList = MTable.getTables.list()(dbsess);
        val tableMap = tableList.map { t ⇒ (t.name.name, t) }.toMap;
        tableMap;
      }
      // Connect to the database and execute the following block within a session

      val tables = makeTableMap
      if (!tables.contains(Experiments.tableName)) { Experiments.ddl create }
      if (!tables.contains(Iterations.tableName)) { Iterations.ddl create }
      if (!tables.contains(AssumedLiterals.tableName)) { AssumedLiterals.ddl create }
    }
  }

  def newIteration(expId : Int, pId : Int, its : Int, level : Int, assuming : List[Int]) : Int = synchronized {
    db withSession {
      val ts = new Timestamp(System.currentTimeMillis())
      val q =
        if (pId <= 0) {
          Iterations.newRootIt.insert(expId, its, level, ts)
          for (it ← Iterations if it.experimentId === expId && it.parentIterationId.isNull && it.remainingIterations === its && it.level === level && it.start === ts) yield it.id
        }
        else {
          Iterations.newIt.insert(expId, Option(pId), its, level, ts)
          for (it ← Iterations if it.experimentId === expId && it.parentIterationId === pId && it.remainingIterations === its && it.level === level && it.start === ts) yield it.id
        }
      if (q.list().length != 1) throw new Exception("Ooops, algo salió mal al recuperar el id de la iteración")
      val id = q.first

      // Save assumed literals
      // @TODO is this more efficient than "assuming.foreach(AssumedLiterals.*.insert(id, _))" ?
      AssumedLiterals.*.insertAll(assuming.map((id, _)) : _*)

      id
    }
  }

  def newExperiment(path : String,
                    iterations : Int,
                    propagationsBudget : Int,
                    conflictsBudget : Int,
                    timeBudget : Double,
                    lifterName : String,
                    filterName : String,
                    keepLearntsLimit : Boolean,
                    keepRestarts : Boolean,
                    keepLearntFacts : Boolean) : Int = synchronized {
    db withSession {
      val ts = new Timestamp(System.currentTimeMillis())
      val host = InetAddress.getLocalHost().getHostName
      Experiments.newExp.insert(path,
        iterations,
        propagationsBudget,
        conflictsBudget,
        timeBudget,
        lifterName,
        filterName,
        host,
        ts,
        keepLearntsLimit,
        keepRestarts,
        keepLearntFacts)

      // Retrieve auto-generated id
      val q = for (e ← Experiments if e.cnf === path && e.host === host && e.start === ts) yield e.id
      // Should be only one
      if (q.list.length != 1) throw new Exception("Ooops, algo falló al insertar en la base de datos")
      q.first
    }
  }

  def updateExpTime(id : Int, result : Char, time : Double) : Timestamp = {
    db withSession {
      // Update solving result and total elapsed time (in seconds)
      val uq = for (e ← Experiments if e.id === id) yield e.result ~ e.totalTime ~ e.end
      val t = new Timestamp(System.currentTimeMillis())
      uq.update((result.toString, Option(time), Option(t)))
      t
    }
  }

  def updateItTime(id : Int, result : Char, time : Double) : Timestamp = {
    db withSession {
      // Update iteration with solving result and total elapsed time
      val qu = for (it ← Iterations if it.id === id) yield it.result ~ it.totalTime ~ it.end
      val t = new Timestamp(System.currentTimeMillis)
      qu.update(result.toString, Option(time), Option(t))
      t
    }
  }

  def criticalTime(itId : Int) : Option[Double] = {
    db withSession {
      val q2 = for (i ← Iterations if i.id === itId) yield i.totalTime
      val myTime = q2.first()
      myTime match {
        case None ⇒ None
        case Some(t) ⇒ {
          val q = for (i ← Iterations if i.parentIterationId === itId) yield i.id
          val l = q.list()

          val children = if (l.size > 0) { l.map(criticalTime _).foldLeft[Option[Double]](Some(0.0))((a, b) ⇒ if (a == None || b == None) None else Some( a.get.max(b.get))) }
          else Some(0.0)

          if (children == None) Some(t)
          else Some(t + children.get)
        }
      }
    }
  }
  
  def totalTime(itId : Int) : Option[Double] = {
    db withSession {
      val q = for {
          i <- Iterations
          i2 <- Iterations if i.id === itId && i2.experimentId === i.experimentId} yield i2.totalTime.sum
      q.first
    }
  }

  def times() = {
    db withSession {
      val q = for (i ← Iterations if i.parentIterationId.isNull) yield i.id
      q.list.map(a ⇒ (a, criticalTime(a), totalTime(a)))
    }
  }

}
