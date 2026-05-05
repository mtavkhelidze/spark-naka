package naka

import naka.ops.epoch.EpochUdf

package object ops {
  val list: List[UdfNaka] = List(
    EpochUdf,
  )
}
