package naka
package spark

import naka.ops.ExprNaka
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.{
  Attribute,
  AttributeReference,
  Expression,
  UnsafeProjection,
}
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.vectorized.ColumnarBatch

case class PhysicalNaka(exps: Seq[ExprNaka], child: SparkPlan)
    extends SparkPlan
    with Logging {

  override def supportsColumnar: Boolean = true
  override def children: Seq[SparkPlan] = child :: Nil

  override lazy val output: Seq[Attribute] =
    child.output ++ exps.map(e =>
      AttributeReference(e.prettyName, e.dataType, e.nullable)(e.exprId),
    )

  private lazy val indexNaka: Seq[(Int, ExprNaka)] = exps.map { e =>
    child.output.indexWhere(_.exprId == e.references.head.exprId) -> e
  }

  override protected def doExecuteColumnar(): RDD[ColumnarBatch] = {
    logDebug(s"doExecuteColumnar")
    child.executeColumnar().mapPartitions { batches =>
      batches.map(batch => {
        val childCols = (0 until batch.numCols).map(batch.column)
        val nakaCols = indexNaka.map { case (srcIdx, exp) =>
          exp.invoke(batch.column(srcIdx), batch.numRows)
        }
        new ColumnarBatch((childCols ++ nakaCols).toArray, batch.numRows)
      })
    }
  }

  // non-columnar
  private lazy val outputExprs: Seq[Expression] =
    child.output ++ exps

  override protected def doExecute(): RDD[InternalRow] = {
    logDebug(s"doExecute")
    child
      .execute()
      .mapPartitions(_.map(UnsafeProjection.create(outputExprs, child.output)))
  }

  override protected def withNewChildrenInternal(
      newChildren: IndexedSeq[SparkPlan],
  ): SparkPlan = copy(child = newChildren.head)
}
