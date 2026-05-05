package naka
package panama

import naka.ops.UdfNaka
import naka.util.ErrorNaka
import org.apache.spark.internal.Logging

import java.io._
import java.lang.foreign.{Linker, SymbolLookup}
import java.lang.invoke.MethodHandle
import java.nio.charset._
import java.nio.file._
import scala.util._
import scala.util.chaining._

object TailorNaka extends Logging {
  private lazy val linker: Either[Throwable, Linker] = Try(
    Linker.nativeLinker(),
  ).toEither
  private lazy val lookup: Either[Throwable, SymbolLookup] = Try(
    SymbolLookup.loaderLookup(),
  ).toEither

  def registerNativeHandles(opsList: List[UdfNaka]): Either[Throwable, Unit] = {
    opsList
      .map(_.nativeName)
      .filter(_.isDefined)
      .map(_.get)
      .map(name =>
        getHandle(name)
          .map(HandleNaka.register(name, _))
          .tap(_.foreach(_ => logDebug(s"Registered: $name"))),
      )
      .partitionMap(identity)
      .pipe { case (errors, _) =>
        if (errors.isEmpty) Right(())
        else Left(ErrorNaka(errors.mkString("\n")))
      }
  }

  private def getHandle(
      name: String,
  ): Either[String, MethodHandle] = lookup
    .flatMap(l => Try(l.find(name).orElseThrow()).toEither)
    .flatMap(sym =>
      linker.map(_.downcallHandle(sym, HandleNaka.nativeDescriptor)),
    )
    .left
    .map(_.getMessage)

  def loadNative: Either[Throwable, Unit] = getLibName
    .flatMap(n => mkTmp(n).flatMap(into => installLib(n, into)))
    .flatMap(p => Try(System.load(p.toAbsolutePath.toString)).toEither)
    .tap(_.foreach(_ => logDebug("Native library loaded.")))

  private def getLibName: Either[Throwable, String] =
    resourceStream("/native/platform")
      .flatMap(Using(_)(_.readAllBytes).toEither)
      .map(new String(_, StandardCharsets.UTF_8).trim)
      .tap(_.foreach(s => logDebug(s"Native library: $s")))

  private def mkTmp(fname: String): Either[Throwable, Path] =
    Try(Files.createTempFile("libnaka-", "-" + fname)).toEither
      .tap(_.foreach(tmp => logDebug(s"Temp file created: $tmp")))

  private def installLib(name: String, into: Path): Either[Throwable, Path] =
    resourceStream(s"/native/$name")
      .flatMap(
        Using(_)(
          Files.copy(_, into, StandardCopyOption.REPLACE_EXISTING),
        ).toEither
          .map(_ => into),
      )
      .tap(_.foreach(s => logDebug(s"Native library installed into: $s")))

  private def resourceStream(path: String): Either[Throwable, InputStream] =
    Option(getClass.getResourceAsStream(path))
      .toRight(new RuntimeException(s"Resource not found: $path"))
}
