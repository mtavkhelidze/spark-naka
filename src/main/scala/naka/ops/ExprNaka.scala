package naka
package ops

import org.apache.spark.sql.catalyst.expressions.{ Expression, ExprId }
import org.apache.spark.sql.vectorized.ColumnVector

trait ExprNaka extends Expression {
  def exprId: ExprId
  def invoke(cv: ColumnVector, nr: Int): ColumnVector
}
