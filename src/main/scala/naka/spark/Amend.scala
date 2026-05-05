package naka.spark

import naka.ops.ExprNaka
import org.apache.spark.sql.catalyst.expressions.{Attribute, AttributeReference}
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.plans.QueryPlan
import org.apache.spark.sql.execution.SparkPlan

object Amend {
  def output(
      childPlan: QueryPlan[?],
      exps: Seq[ExprNaka],
  ): Seq[Attribute] =
    childPlan.output ++ exps.map(e =>
      AttributeReference(e.prettyName, e.dataType)(e.exprId),
    )
}
