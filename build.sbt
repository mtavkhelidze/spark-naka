ThisBuild / scalaVersion := "2.13.18"
ThisBuild / version := "0.1.0"

ThisBuild / description := "Spark Shuffle Optimiser"
ThisBuild / organization := "io.github.mtavkhelidze"
ThisBuild / developers := List(
  Developer(
    id = "mtavkhelidze",
    name = "Misha Tavkhelidze",
    email = "misha.tavkhelidze@gmail.com",
    url = url("https://github.com/mtavkhelidze"),
  ),
)

Global / excludeLintKeys += idePackagePrefix
Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / scalacOptions ++= Seq("-Wconf:src=src_managed/.*:s")

lazy val sparkDeps = Seq(
  "org.apache.spark" % "spark-core_2.13" % "4.1.1",
  "org.apache.spark" % "spark-sql-api_2.13" % "4.1.1",
  "org.apache.spark" % "spark-sql_2.13" % "4.1.1",
)
lazy val otherDeps = Seq(
  "com.beachape" %% "enumeratum" % "1.9.7",
)
lazy val testDeps = Nil

lazy val deps = sparkDeps ++ otherDeps ++ testDeps

lazy val root = (project in file("."))
  .settings(
    fork := true,
    javacOptions := Seq("-Xlint:-options"),
    javaOptions := Seq(
      "--enable-native-access=ALL-UNNAMED",
      "--enable-preview",
      "-XX:MaxJavaStackTraceDepth=10",
    ),
    libraryDependencies ++= deps,
    name := "SparkNaka",
    outputStrategy := Some(StdoutOutput),
    PanamaBuild.settings,
  )
