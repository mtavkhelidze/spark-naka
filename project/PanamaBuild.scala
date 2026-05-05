import sbt.{Def, *}
import sbt.Keys.*

import scala.sys.process.*

object PanamaBuild {
  final lazy val libFileName = s"libnaka.${nativeLibExt}"
  final val nativeDirName = "native"
  final val platformFileName = "platform"
  final val panamaLibsDirName = "/panama/naka/.libs"

  private lazy val nativeLibExt = {
    val os = System.getProperty("os.name").toLowerCase
    if (os.contains("mac")) "dylib"
    else if (os.contains("linux")) "so"
    else throw new RuntimeException(s"Unsupported OS: $os")
  }

  lazy val panamaBuild = taskKey[Unit]("Build native Panama library")
  lazy val panamaClean = taskKey[Unit]("Clean native Panama library")

  private def panamaBuildTask: Def.Initialize[Task[Unit]] = Def.task {
    val log = streams.value.log
    val resDir = (Compile / resourceDirectory).value / nativeDirName
    val platformFile = resDir / platformFileName
    val libsDir = baseDirectory.value / panamaLibsDirName

    val result = Process("make", baseDirectory.value / "panama").!
    if (result != 0) sys.error("Panama build failed")
    log.info(s"${libFileName} build succeeded.")

    val src = libsDir / libFileName
    val dst = resDir / libFileName
    IO.createDirectory(resDir)
    IO.copyFile(src, dst)
    IO.write(platformFile, libFileName)
    log.info(s"Copied to $dst")
  }

  private def panamaCleanTask: Def.Initialize[Task[Unit]] = Def.task {
    val log = streams.value.log
    val resDir = (Compile / resourceDirectory).value / nativeDirName
    val platformFile = resDir / platformFileName

    val result = Process("make clean", baseDirectory.value / "panama").!
    if (result != 0) sys.error("Panama clean failed")

    IO.delete(resDir / libFileName)
    IO.delete(platformFile)
  }

  lazy val settings: Seq[Setting[?]] = Seq(
    panamaBuild := panamaBuildTask.value,
    panamaClean := panamaCleanTask.value,
    clean := (clean dependsOn panamaClean).value,
    Compile / resourceGenerators += Def.task {
      panamaBuild.value
      Seq(
        (Compile / resourceDirectory).value / nativeDirName / platformFileName,
        (Compile / resourceDirectory).value / nativeDirName / libFileName,
      )
    }.taskValue,
  )
}
