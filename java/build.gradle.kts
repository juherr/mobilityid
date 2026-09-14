import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
  `java-library`
  `maven-publish`
  signing
  alias(libs.plugins.spotless)
  alias(libs.plugins.errorprone)
  alias(libs.plugins.dependencycheck)
}

group = "dev.juherr.mobilityid"
version = providers.gradleProperty("releaseVersion").orElse("0.1.0-SNAPSHOT").get()

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
  withSourcesJar()
  withJavadocJar()
}

repositories {
  mavenCentral()
}

dependencies {
  // JSpecify annotations are part of the public API (runtime retention, read by Kotlin and
  // static analyzers on the consumer side), hence `api` as recommended by JSpecify.
  api(libs.jspecify)

  errorprone(libs.errorprone.core)
  errorprone(libs.nullaway)

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.assertj.core)
  testImplementation(libs.jqwik)
  testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(21)
  options.encoding = "UTF-8"
  options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))

  options.errorprone {
    check("EqualsGetClass", CheckSeverity.ERROR)
    check("EqualsIncompatibleType", CheckSeverity.ERROR)
    check("FutureReturnValueIgnored", CheckSeverity.ERROR)
    check("MissingOverride", CheckSeverity.ERROR)
    check("NullAway", CheckSeverity.ERROR)
    check("ReferenceEquality", CheckSeverity.ERROR)
    option("NullAway:AnnotatedPackages", "dev.juherr.mobilityid4j")
    option("NullAway:JSpecifyMode", "true")
  }
}

tasks.withType<Javadoc>().configureEach {
  (options as StandardJavadocDocletOptions).apply {
    addBooleanOption("Xdoclint:all", true)
    addBooleanOption("Werror", true)
  }
}

tasks.withType<AbstractArchiveTask>().configureEach {
  isPreserveFileTimestamps = false
  isReproducibleFileOrder = true
}

// Release safety: refuse to sign/upload a SNAPSHOT and require signing inputs.
val verifyRelease = tasks.register("verifyRelease") {
  val projectVersion = version.toString()
  val signingConfigured = providers.gradleProperty("signingKey")
    .orElse(providers.environmentVariable("SIGNING_KEY")).isPresent &&
    providers.gradleProperty("signingPassword")
      .orElse(providers.environmentVariable("SIGNING_PASSWORD")).isPresent
  doLast {
    if (projectVersion.endsWith("-SNAPSHOT")) {
      throw GradleException("Release publishing requires a non-SNAPSHOT version")
    }
    if (!signingConfigured) {
      throw GradleException("Release publishing requires signingKey/SIGNING_KEY and signingPassword/SIGNING_PASSWORD")
    }
  }
}

// Guard the upload itself, not only the lifecycle task that wraps it.
tasks.matching { it.name.startsWith("nmcpPublish") && it.name.contains("CentralPortal") }.configureEach {
  dependsOn(verifyRelease)
}

dependencyCheck {
  format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.ALL.toString()
  failBuildOnCVSS = 9.0f
  suppressionFile = "${rootDir}/.github/dependency-check-suppressions.xml"

  nvd {
    apiKey.set(System.getenv("NVD_API_KEY"))
  }
}

tasks.named("dependencyCheckAnalyze") {
  notCompatibleWithConfigurationCache("OWASP Dependency-Check plugin is not compatible with configuration cache")
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      artifactId = "mobilityid4j"

      pom {
        name.set("mobilityid4j")
        description.set("Java 21 mobility ID parsing and conversion library")
        url.set("https://github.com/juherr/mobilityid")

        licenses {
          license {
            name.set("Apache License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0")
          }
        }

        developers {
          developer {
            id.set("juherr")
            name.set("Julien Herr")
            email.set("github@juherr.dev")
          }
        }

        scm {
          connection.set("scm:git:git@github.com:juherr/mobilityid.git")
          developerConnection.set("scm:git:git@github.com:juherr/mobilityid.git")
          url.set("https://github.com/juherr/mobilityid")
        }
      }
    }
  }

}

signing {
  val signingKey = providers.gradleProperty("signingKey").orElse(providers.environmentVariable("SIGNING_KEY"))
  val signingPassword = providers.gradleProperty("signingPassword").orElse(
    providers.environmentVariable("SIGNING_PASSWORD")
  )

  if (signingKey.isPresent && signingPassword.isPresent) {
    useInMemoryPgpKeys(signingKey.get(), signingPassword.get())
    sign(publishing.publications)
  }
}

spotless {
  java {
    licenseHeader("""/*
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
""")
    palantirJavaFormat(libs.versions.palantir.java.format.get())
    target("src/*/java/**/*.java")
    formatAnnotations()
    removeUnusedImports()
    trimTrailingWhitespace()
    endWithNewline()
  }
}
