package naka
package spark

import naka.ops.ExprNaka
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.{
  Attribute,
  Expression,
  UnsafeProjection,
}
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.vectorized.ColumnarBatch

case class PhysicalNaka(exps: Seq[ExprNaka], child: SparkPlan)
    extends SparkPlan
    with Logging {

  override lazy val output: Seq[Attribute] = child.output

  override lazy val supportsColumnar: Boolean = true
  override def children: Seq[SparkPlan] = child :: Nil

  private lazy val outputExprs: Seq[Expression] = output.map { a =>
    exps.find(_.exprId == a.exprId).getOrElse(a)
  }

  private def indexNaka =
    outputExprs.collect { case e: ExprNaka =>
      child.output
        .indexWhere(_.exprId == e.references.head.exprId) -> e

    }.toMap

  override protected def doExecuteColumnar(): RDD[ColumnarBatch] = {
    logDebug("doExecuteColumnar")
    child.executeColumnar().mapPartitions { batches =>
      batches.map(batch => {
        val passThroughCols = (0 until batch.numCols).map(batch.column)
        val nakaCols = indexNaka.map { case (idx, exp) =>
          exp.invoke(batch.column(idx), batch.numRows)
        }
        new ColumnarBatch((passThroughCols ++ nakaCols).toArray, batch.numRows)
      })
    }
  }

  override protected def doExecute(): RDD[InternalRow] = {
    child
      .execute()
      .mapPartitions(_.map(UnsafeProjection.create(outputExprs, child.output)))
  }

  override protected def withNewChildrenInternal(
      newChildren: IndexedSeq[SparkPlan],
  ): SparkPlan = copy(child = newChildren.head)
}
