val scala213 = "2.13.18"
val scala3Lts = "3.9.0"

scalaVersion := scala3Lts
crossScalaVersions := Seq(scala213, scala3Lts)

// The repository version, injected by the release workflow (RELEASE_VERSION); a SNAPSHOT otherwise.
version := sys.env.get("RELEASE_VERSION").orElse(sys.props.get("releaseVersion")).getOrElse("0.1.0-SNAPSHOT")

// Release guard, mirrored from java/: refuses a SNAPSHOT and requires the Central Portal
// credentials and a GPG secret key before anything is signed or uploaded.
lazy val verifyRelease = taskKey[Unit]("Fails when the version or the signing/publishing inputs are not release-ready")

val commonSettings = Seq(
  organization := "dev.juherr.mobilityid",
  organizationName := "Julien Herr",
  organizationHomepage := Some(url("https://github.com/juherr")),
  homepage := Some(url("https://github.com/juherr/mobilityid")),
  description := "Parse, validate and convert electric mobility identifiers (ISO 15118-1, DIN SPEC 91286, EMI3)",
  licenses := List(License.Apache2),
  scmInfo := Some(
    ScmInfo(url("https://github.com/juherr/mobilityid"), "scm:git:git@github.com:juherr/mobilityid.git")
  ),
  developers := List(
    Developer(id = "juherr", name = "Julien Herr", email = "julien@herr.fr", url = url("https://github.com/juherr"))
  ),
  versionScheme := Some("early-semver"),
  pomIncludeRepository := { _ => false },
  publishMavenStyle := true,
  // Releases are staged locally and uploaded by `sonaRelease`; SMOKE_REPOSITORY (a directory)
  // redirects `publish` to an isolated Maven layout for the consumer smoke test.
  publishTo :=
    sys.env.get("SMOKE_REPOSITORY").map(dir => MavenCache("smoke-publish", file(dir))).orElse(localStaging.value),
  resolvers ++= sys.env.get("SMOKE_REPOSITORY").map(dir => "smoke" at file(dir).toURI.toString).toSeq,
  // Def.uncached: the guard reads the environment and the GPG keyring, which sbt 2's task
  // cache cannot see; a cached success must never stand in for a real check.
  verifyRelease := Def.uncached {
    val log = streams.value.log
    val v = version.value
    if (v.endsWith("-SNAPSHOT")) sys.error(s"Release publishing requires a non-SNAPSHOT version, got $v")
    val missing = Seq("SONATYPE_USERNAME", "SONATYPE_PASSWORD", "PGP_PASSPHRASE")
      .filterNot(k => sys.env.get(k).exists(_.nonEmpty))
    if (missing.nonEmpty) sys.error(s"Release publishing requires ${missing.mkString(", ")} in the environment")
    val secretKeys = scala.sys.process.Process(Seq("gpg", "--batch", "--with-colons", "--list-secret-keys")).!!
    if (!secretKeys.linesIterator.exists(_.startsWith("sec:")))
      sys.error("Release publishing requires a GPG secret key in the keyring")
    log.info(s"Release inputs verified for $v")
  },
  PgpKeys.publishSigned := PgpKeys.publishSigned.dependsOn(verifyRelease).value,
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
  // ../scripts/mima-baseline.sh and passes it as -Dmobilityid.mimaBaseline=X.Y.Z; without it (no
  // release yet, or a local run) the check is skipped.
  mimaPreviousArtifacts :=
    sys.props.get("mobilityid.mimaBaseline").map(organization.value %% moduleName.value % _).toSet,
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
