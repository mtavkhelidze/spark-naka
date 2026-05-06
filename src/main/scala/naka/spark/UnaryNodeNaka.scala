package naka
package spark

import naka.ops.ExprNaka
import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.catalyst.plans.logical.{ LogicalPlan, UnaryNode }

case class UnaryNodeNaka(
    exps: Seq[ExprNaka],
    override val child: LogicalPlan,
) extends UnaryNode
    with LogicalNaka
    with Logging {

  override lazy val output: Seq[Attribute] = child.output

  override protected def withNewChildInternal(nc: LogicalPlan): LogicalPlan =
    copy(child = nc)
}
