package naka
package spark

import naka.ops.ExprNaka
import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.expressions.{Attribute, AttributeReference}
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, UnaryNode}

case class UnaryNodeNaka(
    exps: Seq[ExprNaka],
    override val child: LogicalPlan,
) extends UnaryNode
    with LogicalPlanNaka
    with Logging {

  override lazy val output: Seq[Attribute] = {
    val res = child.output ++ exps.map(e =>
      AttributeReference(e.prettyName, e.dataType)(e.exprId),
    )
    logDebug(s"output: ${res}")
    res
  }

  override protected def withNewChildInternal(nc: LogicalPlan): LogicalPlan =
    copy(child = nc)
}
