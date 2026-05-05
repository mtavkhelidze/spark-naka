package naka
package panama

import java.lang.foreign._

object StdLib {
  type MemPage = MemorySegment

  object sizeof {
    final val uint8_t: Long = 1L
    final val uint16_t: Long = 2L
    final val uint64_t: Long = 8L
    private final val fixnum_pad: Long = 6L
    private final val varbyte_pad: Long = 0L
    final val fixnum_header: Long = uint16_t + fixnum_pad
    final val varByte_header: Long = uint16_t + varbyte_pad
  }
  def malloc(size: Long)(implicit arena: Arena): MemPage =
    arena.allocate(size)

  def memcopy(dst: MemPage, src: MemPage, size: Long): Unit =
    MemorySegment.copy(src, 0L, dst, 0L, size)

}
