package naka.util

import scala.util.control.NoStackTrace

case class ErrorNaka(msg: String = "Stop!")
    extends Throwable(msg)
    with NoStackTrace
