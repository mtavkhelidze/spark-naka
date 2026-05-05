package naka.spark

import naka.ops.ExprNaka
import org.apache.spark.sql.catalyst.expressions.Attribute

object Remap {
  def output(
      ofChild: Seq[Attribute],
      withExps: Seq[ExprNaka],
  ): Seq[Attribute] = {
    val replacements =
      withExps.map(e => e.references.head.exprId -> e.dataType).toMap
    ofChild.map(attr =>
      replacements.get(attr.exprId).fold(attr)(attr.withDataType),
    )
  }
}
