package naka
package spark

import naka.ops.ExprNaka
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, Project}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.NamedExpression

case class LogicalRuleNaka(spark: SparkSession) extends Rule[LogicalPlan] {
  def apply(plan: LogicalPlan): LogicalPlan = plan transform ({
    case orig @ Project(projectList, child) =>
      if (child.isInstanceOf[LogicalNaka]) orig
      else {
        val nakas = collectNakas(projectList)
        if (nakas.isEmpty) orig
        else {
          val node = UnaryNodeNaka(nakas, child)
          val rewritten =
            projectList.map(_.transform { case e: ExprNaka =>
              node.output
                .find(_.exprId == e.exprId)
                .getOrElse(e)
            }.asInstanceOf[NamedExpression])
          Project(rewritten, node)
        }
      }
  })

  private def collectNakas(exps: Seq[NamedExpression]): Seq[ExprNaka] = {
    exps.flatMap(_.collect { case en: ExprNaka => en })
  }
}
