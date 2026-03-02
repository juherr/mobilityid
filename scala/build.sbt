import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._

ThisBuild / scalaVersion := "3.8.2"
ThisBuild / crossScalaVersions := Seq("2.12.21", "2.13.18", "3.3.7", "3.8.1")

val commonSettings = Seq(
  organization := "com.thenewmotion",
  licenses += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
  headerLicense := Some(HeaderLicense.Custom(
    """|Copyright (c) 2014 The New Motion team, and respective contributors
       |Copyright (c) 2026 Julien Herr, and respective contributors
       |
       |Licensed under the Apache License, Version 2.0 (the "License");
       |you may not use this file except in compliance with the License.
       |You may obtain a copy of the License at
       |
       |    http://www.apache.org/licenses/LICENSE-2.0
       |
       |Unless required by applicable law or agreed to in writing, software
       |distributed under the License is distributed on an "AS IS" BASIS,
       |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       |See the License for the specific language governing permissions and
       |limitations under the License.""".stripMargin
  )),
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

val specs2Version = "4.23.0"  // Cross-compatible Scala 2.12/2.13/3.x

val `core` = project
  .settings(
    name := "mobilityid",
    commonSettings,
    Compile / console / initialCommands := "import com.thenewmotion.mobilityid._, ContractIdStandard._",
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % specs2Version % "test"
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
        case Some((2, _)) => Seq("com.propensive" %% "contextual-core" % "3.0.1")
        case _            => Seq.empty
      }
    },
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % specs2Version % "test"
    )
  )

val `mobilityid` =
  project.in(file("."))
    .disablePlugins(de.heikoseeberger.sbtheader.HeaderPlugin)
    .aggregate(
      `core`,
      `interpolators`)
    .settings(
      publish := {}
    )
