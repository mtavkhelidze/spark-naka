package naka
package panama

import naka.panama.StdLib.MemPage
import org.apache.spark.sql.vectorized.ColumnVector

case class VarByte private (
    nRows: Int,
    data: MemPage,
) {
  lazy val size: Long = data.byteSize
}

object VarByte {
  def apply(cv: ColumnVector, nr: Int): VarByte = ???

  private def byteSize(cv: ColumnVector, nRows: Int): Long =
    (0 until nRows).foldLeft(0L) { (acc, i) =>
      acc + 4L + cv.getUTF8String(i).numBytes()
    }
}
