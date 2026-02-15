import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
  `java-library`
  `maven-publish`
  signing
  id("com.diffplug.spotless") version "6.25.0"
  id("net.ltgt.errorprone") version "4.1.0"
  id("org.owasp.dependencycheck") version "12.2.0"
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
  compileOnly("org.jspecify:jspecify:1.0.0")
  testCompileOnly("org.jspecify:jspecify:1.0.0")

  errorprone("com.google.errorprone:error_prone_core:2.28.0")
  errorprone("com.uber.nullaway:nullaway:0.12.3")

  testImplementation(platform("org.junit:junit-bom:5.11.4"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation("org.assertj:assertj-core:3.27.2")
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(21)
  options.encoding = "UTF-8"

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

val verifyRelease by tasks.registering {
  doLast {
    if (version.toString().endsWith("-SNAPSHOT")) {
      throw GradleException("Release publishing requires a non-SNAPSHOT version")
    }

    val required = listOf(
      "mavenCentralPortalUrl" to "MAVEN_CENTRAL_PORTAL_URL",
      "mavenCentralUsername" to "MAVEN_CENTRAL_USERNAME",
      "mavenCentralPassword" to "MAVEN_CENTRAL_PASSWORD",
      "signingKey" to "SIGNING_KEY",
      "signingPassword" to "SIGNING_PASSWORD"
    )

    val missing = required.filter { (propertyName, envName) ->
      !providers.gradleProperty(propertyName).isPresent &&
        !providers.environmentVariable(envName).isPresent
    }

    if (missing.isNotEmpty()) {
      val missingKeys = missing.joinToString(", ") { (propertyName, envName) ->
        "$propertyName/$envName"
      }
      throw GradleException("Release publishing requires credentials/signing inputs: $missingKeys")
    }
  }
}

tasks.withType<PublishToMavenRepository>().configureEach {
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

  repositories {
    val centralPortalUrl =
      providers.gradleProperty("mavenCentralPortalUrl").orElse(
        providers.environmentVariable("MAVEN_CENTRAL_PORTAL_URL")
      )
    val centralPortalUsername =
      providers.gradleProperty("mavenCentralUsername").orElse(
        providers.environmentVariable("MAVEN_CENTRAL_USERNAME")
      )
    val centralPortalPassword =
      providers.gradleProperty("mavenCentralPassword").orElse(
        providers.environmentVariable("MAVEN_CENTRAL_PASSWORD")
      )

    if (centralPortalUrl.isPresent && centralPortalUsername.isPresent && centralPortalPassword.isPresent) {
      maven {
        name = "mavenCentralPortal"
        url = uri(centralPortalUrl.get())

        credentials {
          username = centralPortalUsername.get()
          password = centralPortalPassword.get()
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
    palantirJavaFormat("2.50.0")
    target("src/*/java/**/*.java")
    formatAnnotations()
    removeUnusedImports()
    trimTrailingWhitespace()
    endWithNewline()
  }
}
