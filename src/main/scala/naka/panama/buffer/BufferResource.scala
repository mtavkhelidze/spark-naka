package naka.panama.buffer

import java.lang.foreign.Arena
import scala.util.Using.Releasable

private[buffer] trait BufferResource {
  protected val arena: Arena

  private def close(): Unit = arena.close()
}

object BufferResource {
  implicit val releasable: Releasable[BufferResource] =
    (nb: BufferResource) => nb.close()
}
