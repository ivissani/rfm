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

object Experiments extends Table[(Int, String, Int, Int, Int, Double, String, String, String, Option[Double], String, Timestamp, Option[Timestamp])]("EXPERIMENTS") {
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

  def * = id ~ cnf ~ iterations ~ propagationsBudget ~ conflictsBudget ~ timeBudget ~ lifterClassName ~ filterClassName ~ result ~ totalTime ~ host ~ start ~ end
  def newExp = cnf ~ iterations ~ propagationsBudget ~ conflictsBudget ~ timeBudget ~ lifterClassName ~ filterClassName ~ host ~ start
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

class H2ExperimentLogger extends ExperimentLogger {
  var db : Database = null

  def experimental() {
    var ml = new ParHashSet[Int]
    ml ++= List.range(1, 1000000)

    var c = 0
    def count() = synchronized {
      c += 1
    }

    def f(l : ParHashSet[Int]) {
      l.foreach(a ⇒ {
        Console println a
        count()
        if (a % 10 == 0) List.range(10 * 1000000 + 1, 10 * 1000000 + 10).foreach(l.addEntry(_))
      })
    }

    f(ml)
  }

  def initialize(db : Database) {
    this.db = db
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
                    filterName : String) : Int = synchronized {
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
        ts)

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
}
