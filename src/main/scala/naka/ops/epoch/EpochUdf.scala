package naka
package ops
package epoch

import naka.panama.buffer.ByteBuffer
import naka.panama.NativeNaka
import naka.panama.StdLib._
import naka.util.ErrorNaka
import org.apache.spark.internal.Logging
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.vectorized.ColumnVector
import org.apache.spark.unsafe.types.UTF8String

import java.lang.foreign.ValueLayout
import scala.util.Using

object EpochUdf extends UdfNaka("epoch") with Logging {
  self =>
  override lazy val nativeName: Option[String] = Option("epoch")
  override val className: String = self.getClass.getName.stripSuffix("$")

  override def builder: ExprBuilder = exps => EpochExpr(exps.head, "epoch")

  def epoch(cv: ColumnVector, nr: Int): ColumnVector = {
    val nativeFn = NativeNaka.forName("epoch")
    Using(ByteBuffer.create(LongType, nr))(buf => {
      val input = buf.input(nr, cv).head
      val count = nativeFn(input, buf.output)
      if (count <= 0) {
        throw new ErrorNaka(s"epoch failed: $count")
      }
      val result = VectorNaka.reserve(count, LongType)
      (0 until count).foreach { i =>
        val v = buf.output
          .asSlice(sizeof.fixnum_header)
          .get(ValueLayout.JAVA_LONG_UNALIGNED, 0L + i * sizeof.uint64_t)
        result.putLong(i, v)
      }
      result
    }).get
  }

  def epoch(s: UTF8String): Long = {
    epoch(VectorNaka.from(s), 1)
      .getLong(0)
  }
}
