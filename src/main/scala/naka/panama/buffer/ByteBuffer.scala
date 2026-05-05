package naka.panama.buffer

import naka.panama.StdLib._
import org.apache.spark.sql.types.DataType
import org.apache.spark.sql.vectorized.ColumnVector

import java.lang.foreign._

case class ByteBuffer private (override val arena: Arena, ot: DataType, nr: Int)
    extends BufferResource {

  implicit val arenaImplicit: Arena = arena

  lazy val output: MemPage = ColumnBuffer.outPage(ot, nr)

  def input(nr: Int, cv: ColumnVector, cvs: ColumnVector*): Seq[MemPage] =
    (cv +: cvs).map(ColumnBuffer(_, nr).inPage)
}

object ByteBuffer {
  def create(outType: DataType, nr: Int): ByteBuffer = new ByteBuffer(
    Arena.ofConfined(),
    outType,
    nr,
  )
}
