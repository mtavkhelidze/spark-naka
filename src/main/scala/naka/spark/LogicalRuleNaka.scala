package naka
package spark

import naka.ops.ExprNaka
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, Project}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.NamedExpression

case class LogicalRuleNaka(spark: SparkSession) extends Rule[LogicalPlan] {
  def apply(plan: LogicalPlan): LogicalPlan = plan transform ({
    case orig @ Project(pl, child) =>
      val nakas = collectNakas(pl)
      if (nakas.isEmpty) orig
      else if (child.isInstanceOf[LogicalNaka]) orig
      else Project(rewriteUp(pl, child), UnaryNodeNaka(nakas, child))
  })

  private def rewriteUp(
      ne: Seq[NamedExpression],
      childPlan: LogicalPlan,
  ): Seq[NamedExpression] = ne
    .map(_.transformUp { case naka: ExprNaka =>
      childPlan.output
        .find(_.exprId == naka.exprId)
        .getOrElse(naka)
    }.asInstanceOf[NamedExpression])

  private def collectNakas(exps: Seq[NamedExpression]): Seq[ExprNaka] = {
    exps.flatMap(_.collect { case en: ExprNaka => en })
  }
}
