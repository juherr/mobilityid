ThisBuild / scalaVersion := "2.13.16"
ThisBuild / crossScalaVersions := Seq("2.13.16", "2.12.20", "3.3.7", "3.8.1")

val commonSettings = Seq(
  organization := "com.thenewmotion",
  licenses += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
  javacOptions ++= Seq(
    "-source",
    "1.8",
    "-target",
    "1.8"
  ),
  Compile / doc / javacOptions ++= Seq("-source", "1.8"),
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-unchecked",
    "-deprecation",
    "-feature"
  ) ++ {
    scalaBinaryVersion.value match {
      case "2.12" => Seq("-Xlog-reflective-calls", "-Xlint", "-Ywarn-value-discard",
                         "-Ywarn-unused-import", "-target:jvm-1.8")
      case "2.13" => Seq("-Xlog-reflective-calls", "-Xlint", "-Ywarn-value-discard",
                         "-Ywarn-unused:imports", "-target:jvm-1.8")
      case _      => Seq("-Wvalue-discard", "-Wunused:imports")
    }
  },
  Compile / console / scalacOptions --= Seq("-Ywarn-unused-import", "-Ywarn-unused:imports", "-Wunused:imports"),
  Test / parallelExecution := true,
  Test / fork := true,
  run / fork := true,
  Global / cancelable := true
)

val specs2 = "org.specs2" %% "specs2-core" % "4.20.9" % "test"

val `core` = project
  .settings(
    name := "mobilityid",
    commonSettings,
    Compile / console / initialCommands := "import com.thenewmotion.mobilityid._, ContractIdStandard._",
    libraryDependencies ++= Seq(
      specs2
    )
  )

val `interpolators` = project
  .dependsOn(`core`)
  .settings(
    name := "mobilityid-interpolators",
    commonSettings,
    Compile / unmanagedSourceDirectories ++= {
      val base = (Compile / sourceDirectory).value
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => Seq(base / "scala-2")
        case Some((3, _)) => Seq(base / "scala-3")
        case _            => Seq.empty
      }
    },
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => Seq("com.propensive" %% "contextual-core" % "3.0.0")
        case _            => Seq.empty
      }
    },
    libraryDependencies ++= Seq(
      specs2
    )
  )

val `mobilityid` =
  project.in(file("."))
    .aggregate(
      `core`,
      `interpolators`)
    .settings(
      publish := {}
    )
