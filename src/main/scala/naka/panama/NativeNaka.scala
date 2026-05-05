package naka.panama

import naka.panama.HandleNaka.NativeHandle
import naka.panama.StdLib.MemPage
import naka.util.ErrorNaka
import org.apache.spark.internal.Logging

object NativeNaka extends Logging {

  type NativeFunction = (MemPage, MemPage) => Int

  def forName(name: String): NativeFunction = {
    HandleNaka
      .get(name)
      .map(native(_))
      .getOrElse(throw ErrorNaka(s"No handle for: $name"))
  }

  private def native(handle: NativeHandle): NativeFunction =
    (in: MemPage, out: MemPage) => handle.invokeExact(in, out, out.byteSize)
}
