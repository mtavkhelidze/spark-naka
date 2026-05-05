package naka.panama.buffer

import naka.panama.StdLib._
import org.apache.spark.sql.types._
import org.apache.spark.sql.vectorized.ColumnVector

import java.lang.foreign._

sealed trait ColumnBufferAllocator {
  private[buffer] def allocate(nr: Int)(implicit arena: Arena): MemPage
}

sealed trait ColumnBuffer {
  val inPage: MemPage
}

case class VarBytes(cv: ColumnVector, nr: Int)(implicit arena: Arena)
    extends ColumnBuffer {
  override lazy val inPage = {
    val rows = (0 until nr).map(i => cv.getUTF8String(i).getBytes)
    val totalBytes = rows.foldLeft(0L)(_ + _.length)
    // header + offests + strings
    val mem = malloc(
      (sizeof.varByte_header) + (sizeof.uint16_t * (nr + 1)) + (totalBytes),
    )
    // size
    mem.set(ValueLayout.JAVA_SHORT, 0L, nr.toShort)
    val bytesStart = sizeof.varByte_header + sizeof.uint16_t * (nr + 1)
    // offests and data
    rows.zipWithIndex.foldLeft(0) { case (off, (bytes, i)) =>
      mem.set(
        ValueLayout.JAVA_SHORT,
        sizeof.varByte_header + i * sizeof.uint16_t,
        off.toShort,
      )
      MemorySegment.copy(
        bytes,
        0,
        mem,
        ValueLayout.JAVA_BYTE,
        bytesStart + off,
        bytes.length,
      )
      off + bytes.length
    }
    // Sentinel offset
    mem.set(
      ValueLayout.JAVA_SHORT,
      sizeof.varByte_header + nr * sizeof.uint16_t,
      totalBytes.toShort,
    )
    mem
  }
}

object VarBytes extends ColumnBufferAllocator {
  final val REASONABLE_VARCHAR_LEN = 64

  override def allocate(nr: Int)(implicit arena: Arena): MemPage = {
    val mem = malloc(
      sizeof.varByte_header + (sizeof.uint8_t * REASONABLE_VARCHAR_LEN * nr),
    )
    mem
  }
}

case class FixedNum(cv: ColumnVector, nr: Int)(implicit arena: Arena)
    extends ColumnBuffer {
  override lazy val inPage = {
    val mem = FixedNum.allocate(nr)
    mem.set(ValueLayout.JAVA_SHORT, 0L, nr.toShort)
    (0 until nr).foreach { i =>
      mem.set(
        ValueLayout.JAVA_LONG,
        sizeof.fixnum_header + i * sizeof.uint64_t,
        cv.getLong(i),
      )
    }
    mem
  }
}

object FixedNum extends ColumnBufferAllocator {
  override def allocate(nr: Int)(implicit arena: Arena): MemPage =
    malloc(sizeof.fixnum_header + (sizeof.uint64_t * nr))
}

object ColumnBuffer {
  def outPage(dt: DataType, nr: Int)(implicit arena: Arena): MemPage =
    dt match {
      case LongType => FixedNum.allocate(nr)
      case StringType => VarBytes.allocate(nr)
    }

  def apply(cv: ColumnVector, nr: Int)(implicit arena: Arena): ColumnBuffer =
    cv.dataType match {
      case LongType => new FixedNum(cv, nr)
      case StringType => new VarBytes(cv, nr)
      case _ =>
        throw new IllegalArgumentException(
          s"Unsupported data type: ${cv.dataType}",
        )
    }
}
