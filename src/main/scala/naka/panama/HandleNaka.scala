package naka
package panama

import java.lang.foreign._
import java.lang.invoke.MethodHandle
import scala.collection.mutable

object HandleNaka {
  type NativeHandle = MethodHandle
  final val nativeDescriptor: FunctionDescriptor = {

    /** Defined in <naka/batch.h>
      *
      * auto fn(batch_t* in, batch_t* out) -> int32_t;
      * @param in:
      *   incoming batch
      * @param out:
      *   memory for results
      * @param size:
      *   size of memory for results
      * @return:
      *   number of rows written or negative number if error
      */
    FunctionDescriptor.of(
      ValueLayout.JAVA_INT,
      ValueLayout.ADDRESS,
      ValueLayout.ADDRESS,
      ValueLayout.JAVA_LONG,
    )
  }
  private val handles: mutable.Map[String, NativeHandle] = mutable.Map.empty

  def register(name: String, handle: NativeHandle): Unit = {
    handles += (name -> handle)
  }

  def get(name: String): Option[NativeHandle] = handles.get(name)
}
