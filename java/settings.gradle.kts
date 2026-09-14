plugins {
  // Lets Gradle provision the Java 21 toolchain when it is not installed (required from Gradle 10).
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("com.gradleup.nmcp.settings") version "1.6.2"
}

rootProject.name = "mobilityid4j"

// Plain strings: the settings plugin serializes these into an isolated lifecycle action,
// which does not accept environment-variable providers.
fun releaseInput(propertyName: String, envName: String): String? =
  providers.gradleProperty(propertyName).orNull ?: System.getenv(envName)

nmcpSettings {
  centralPortal {
    username = releaseInput("mavenCentralUsername", "MAVEN_CENTRAL_USERNAME")
    password = releaseInput("mavenCentralPassword", "MAVEN_CENTRAL_PASSWORD")
    publishingType = "AUTOMATIC"
    publicationName = releaseInput("releaseVersion", "RELEASE_VERSION")?.let { "mobilityid4j:$it" } ?: "mobilityid4j"
  }
}
