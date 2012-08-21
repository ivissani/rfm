package ar.uba.dc.rfm.paralloy.scalaframework.lifters

import ar.uba.dc.rfm.paralloy.scalaframework.Minisat
import scala.collection.mutable.MutableList
import scala.collection.mutable.PriorityQueue
import ar.uba.dc.rfm.minisat.intseq
import ar.uba.dc.rfm.paralloy.scalaframework.datatypes.IntSeq

class VarActivityLifter(limit: Int, lessActive: Boolean = false) extends AbstractLifter {

  def variablesToLift() = {
    def ret(m: Minisat): List[Int] = {
      class ActivityOrderedVar(v: Tuple2[Int, Double]) extends Ordered[ActivityOrderedVar] {
        def activity = this.v._2
        def variable = this.v._1

        def compare(that: ActivityOrderedVar) = this.activity.compare(that.activity)
      }

      var heap = new PriorityQueue[ActivityOrderedVar]

      val assumed = new IntSeq(m.get_assumptions(new intseq)).toList().map(_.abs)

      Range(1, m.nVars() + 1).foreach((v: Int) ⇒ if (!assumed.contains(v)) heap.enqueue(new ActivityOrderedVar((v, m.get_var_activity(v)))))

      (if (lessActive) heap.reverse else heap).take(limit).toList.map(_.variable)
    }

    ret
  }
  
  def getCannonicalAndParameterizedName() = {
    val res = getClass().getCanonicalName()
    res + "(limit=%d, lessActive=%c)".format(limit, if(lessActive) 'T' else 'F')
  }
}