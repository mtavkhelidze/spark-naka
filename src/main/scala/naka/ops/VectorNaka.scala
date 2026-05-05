package naka.ops

import org.apache.spark.sql.execution.vectorized.OffHeapColumnVector
import org.apache.spark.sql.types._
import org.apache.spark.sql.vectorized.{ColumnarBatch, ColumnVector}
import org.apache.spark.unsafe.types.UTF8String

object VectorNaka {
  type WriteableVector = OffHeapColumnVector

  def reserve(nr: Int, dt: DataType): WriteableVector =
    new OffHeapColumnVector(nr, dt)

  def from(n: Long): ColumnVector = {
    val cv = reserve(1, LongType)
    cv.putLong(0, n)
    cv
  }

  def from(s: UTF8String): ColumnVector = {
    val col = reserve(1, StringType)
    col.putByteArray(0, s.getBytes)
    col
  }
}
