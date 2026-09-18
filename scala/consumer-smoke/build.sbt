/*
 * Copyright (c) 2014 The New Motion team, and respective contributors
 * Copyright (c) 2026 Julien Herr, and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Throw-away consumer of the published Scala artifacts, the way a downstream build would use
// them: resolved from the isolated Maven repository written by scripts/verify-consumer.sh.
val mobilityidVersion = sys.env.getOrElse("MOBILITYID_VERSION", sys.error("MOBILITYID_VERSION is required"))
val smokeRepository = sys.env.getOrElse("SMOKE_REPOSITORY", sys.error("SMOKE_REPOSITORY is required"))

scalaVersion := "3.9.0"
crossScalaVersions := Seq("2.13.18", "3.9.0")
name := "mobilityid-consumer-smoke"
publish / skip := true
resolvers += "smoke" at file(smokeRepository).toURI.toString
libraryDependencies ++= Seq(
  "dev.juherr.mobilityid" %% "mobilityid" % mobilityidVersion,
  "dev.juherr.mobilityid" %% "mobilityid-interpolators" % mobilityidVersion
)
scalacOptions ++= Seq("-deprecation", "-feature", "-release", "17")
run / fork := true
