package naka.ops
package epoch

import naka.ops.ExprNaka
import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.expressions._
import org.apache.spark.sql.catalyst.expressions.codegen._
import org.apache.spark.sql.types._
import org.apache.spark.sql.vectorized.ColumnVector

case class EpochExpr(
    override val child: Expression,
    name: String,
    override val exprId: ExprId = NamedExpression.newExprId,
) extends UnaryExpression
    with CodegenFallback
    with ExprNaka
    with ImplicitCastInputTypes
    with Logging {

  override lazy val deterministic: Boolean = child.deterministic
  override val dataType: DataType = LongType
  override val inputTypes: Seq[DataType] = StringType :: Nil
  override val prettyName: String = name

  override def nullable: Boolean = child.nullable

  override def doGenCode(
      ctx: CodegenContext,
      ev: ExprCode,
  ): ExprCode = {
    nullSafeCodeGen(
      ctx,
      ev,
      n => {
        val fn = s"${EpochUdf.className}.${name}"
        s"""
           |${ev.value} = $fn($n);
           |""".stripMargin
      },
    )
  }

  override protected def withNewChildInternal(nc: Expression): Expression =
    copy(child = nc)

  override def invoke(cv: ColumnVector, nr: Int): ColumnVector =
    EpochUdf.epoch(cv, nr)
}
