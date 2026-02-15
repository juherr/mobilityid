ThisBuild / scalaVersion := "2.13.16"
ThisBuild / crossScalaVersions := Seq("2.13.16", "2.12.20")

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
    "-feature",
    "-Xlog-reflective-calls",
    "-Xlint",
    "-Ywarn-value-discard"
  ) ++ {
    scalaBinaryVersion.value match {
      case "2.12" => Seq("-Ywarn-unused-import", "-target:jvm-1.8")
      case "2.13" => Seq("-Ywarn-unused:imports", "-target:jvm-1.8")
      case _ => Seq.empty
    }
  },
  Compile / console / scalacOptions --= Seq("-Ywarn-unused-import", "-Ywarn-unused:imports"),
  Test / parallelExecution := true,
  Test / fork := true,
  run / fork := true,
  Global / cancelable := true
)

val specs2 = "org.specs2" %% "specs2-core" % "4.10.5" % "test"

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
    libraryDependencies ++= Seq(
      "com.propensive" %% "contextual-core" % "3.0.0",
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
