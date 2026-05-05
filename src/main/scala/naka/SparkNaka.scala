package naka

import naka.panama.TailorNaka
import naka.spark._
import naka.util.InitNaka
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSessionExtensions
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.execution._

import scala.util.Try

class SparkNaka extends (SparkSessionExtensions => Unit) with Logging {
  def apply(extensions: SparkSessionExtensions): Unit = {
    init(extensions).fold(
      t => logError(s"Failed to initialize: $t"),
      _ => logInfo(s"Plugin loaded"),
    )
  }

  private def init: InitNaka.Result = ex => {
    List(
      loadNative,
      injectFunctions,
      injectLogicalRule,
      injectPhysical,
    )
      .foldLeft(Right(()): Either[Throwable, Unit])((acc, f) =>
        acc.flatMap(_ => f(ex)),
      )
  }
  private def loadNative: InitNaka.Result = _ => {
    TailorNaka.loadNative
      .flatMap(_ => TailorNaka.registerNativeHandles(ops.list))
  }

  private def injectFunctions: InitNaka.Result = ex => {
    ops.list.foreach(op => ex.injectFunction(op.sparkDescriptor))
    Right(())
  }

  private def injectLogicalRule: InitNaka.Result = ex =>
    Try(ex.injectPostHocResolutionRule(LogicalRuleNaka)).toEither

  private def injectPhysical: InitNaka.Result = ex =>
    Try(ex.injectPlannerStrategy { _ =>
      new SparkStrategy {
        override def apply(plan: LogicalPlan): Seq[SparkPlan] = plan match {
          case UnaryNodeNaka(exps, child) => {
            PhysicalNaka(exps, planLater(child)) :: Nil
          }
          case _ => Nil
        }
      }
    }).toEither

}
