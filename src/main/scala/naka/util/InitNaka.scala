package naka
package util

import org.apache.spark.sql.SparkSessionExtensions

object InitNaka {
  type Result = SparkSessionExtensions => Either[Throwable, Unit]
}
