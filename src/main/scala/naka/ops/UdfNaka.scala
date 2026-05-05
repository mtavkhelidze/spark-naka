package naka
package ops

import org.apache.spark.sql.SparkSessionExtensions
import org.apache.spark.sql.catalyst.expressions.{Expression, ExpressionInfo}
import org.apache.spark.sql.catalyst.FunctionIdentifier

abstract class UdfNaka(name: String) {
  protected type ExprBuilder = Seq[Expression] => ExprNaka
  private type SparkDescriptor = SparkSessionExtensions#FunctionDescription

  val className: String
  val nativeName: Option[String]
  val sparkDescriptor: SparkDescriptor = (
    FunctionIdentifier(name),
    new ExpressionInfo(classOf[UdfNaka].getName, name),
    builder,
  )

  def builder: ExprBuilder
}
