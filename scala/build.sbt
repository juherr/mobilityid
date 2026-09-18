val scala213 = "2.13.18"
val scala3Lts = "3.9.0"

scalaVersion := scala3Lts
crossScalaVersions := Seq(scala213, scala3Lts)

val commonSettings = Seq(
  organization := "com.thenewmotion",
  licenses := List(License.Apache2),
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
  // JDK 17 is the bytecode baseline: Scala 3.9 LTS needs JDK 17+ to compile and run anyway.
  javacOptions ++= Seq("--release", "17"),
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-release",
    "17"
  ) ++ {
    scalaBinaryVersion.value match {
      // unused-nowarn is silenced: a @nowarn needed by Scala 3 only would be reported here.
      case "2.13" =>
        Seq("-Xlint", "-Wvalue-discard", "-Wunused:imports", "-Wconf:cat=unused-nowarn:s", "-Xfatal-warnings")
      case _ => Seq("-Wvalue-discard", "-Wunused:imports", "-Werror")
    }
  },
  Compile / console / scalacOptions --= Seq("-Wunused:imports", "-Xfatal-warnings", "-Werror"),
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  // Binary compatibility against the last release on Maven Central. CI resolves the baseline with
  // scripts/mima-baseline.sh and passes it as -Dmobilityid.mimaBaseline=X.Y.Z; without it (no
  // release yet, or a local run) the check is skipped.
  mimaPreviousArtifacts := sys.props.get("mobilityid.mimaBaseline").map(organization.value %% moduleName.value % _).toSet,
  mimaFailOnNoPrevious := false,
  Test / parallelExecution := true,
  Test / fork := true,
  run / fork := true,
  Global / cancelable := true
)

val specs2Version = "4.23.0" // 5.x is Scala 3 only; the shared specs must also run on 2.13.

lazy val core = project
  .settings(
    name := "mobilityid",
    commonSettings,
    Compile / console / initialCommands := "import com.thenewmotion.mobilityid._, ContractIdStandard._",
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % specs2Version % Test
    )
  )

lazy val interpolators = project
  .dependsOn(core)
  .settings(
    name := "mobilityid-interpolators",
    commonSettings,
    // src/main/scala-2 (contextual-core macros) and scala-3 (inline macros) are picked up by sbt.
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => Seq("com.propensive" %% "contextual-core" % "3.0.1")
        case _ => Seq.empty
      }
    },
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % specs2Version % Test
    )
  )

lazy val root = project
  .in(file("."))
  .disablePlugins(sbtheader.HeaderPlugin)
  .aggregate(core, interpolators)
  .settings(
    publish / skip := true,
    mimaFailOnNoPrevious := false
  )
