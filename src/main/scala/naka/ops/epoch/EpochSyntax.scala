package naka.ops
package epoch

import org.apache.spark.internal.Logging
import org.apache.spark.sql.Column
import org.apache.spark.sql.functions._

trait EpochSyntax extends Logging {
  def epoch(name: String): Column =
    call_function("epoch", col(name))

  def epoch(c: Column): Column =
    call_function("epoch", c)
}

object EpochSyntax extends EpochSyntax {
  implicit class EpochOps(val c: Column) extends AnyVal {
    def epoch: Column = {
      logDebug(s"epoch_column_impl(${c})")
      call_function("epoch", c)
    }
  }
}
