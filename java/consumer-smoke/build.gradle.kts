// Consumes the published mobilityid4j artifact (POM, module metadata, JPMS descriptor) from an
// isolated repository, the way a downstream application would. Driven by scripts/verify-consumer.sh.
plugins {
  application
  kotlin("jvm") version "2.4.20"
}

val mobilityidVersion = providers.gradleProperty("mobilityidVersion")
val mobilityidRepository = providers.gradleProperty("mobilityidRepository")

repositories {
  exclusiveContent {
    forRepository {
      maven {
        name = "smoke"
        url = uri(mobilityidRepository.get())
      }
    }
    filter { includeGroup("dev.juherr.mobilityid") }
  }
  mavenCentral()
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

dependencies {
  implementation("dev.juherr.mobilityid:mobilityid4j:${mobilityidVersion.get()}")
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(21)
  options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

kotlin {
  jvmToolchain(21)
  compilerOptions {
    allWarningsAsErrors.set(true)
    // JSpecify nullness must be enforced, not just reported: a wrong contract fails the build.
    freeCompilerArgs.addAll("-Xjspecify-annotations=strict")
  }
}

application {
  mainModule.set("dev.juherr.mobilityid4j.smoke")
  mainClass.set("dev.juherr.mobilityid4j.smoke.Smoke")
}

val runKotlin = tasks.register<JavaExec>("runKotlin") {
  description = "Runs the Kotlin consumer smoke."
  group = "verification"
  // Kotlin classes are not patched into the JPMS module: the Kotlin smoke runs on the classpath.
  // The JSpecify check it carries is a compile-time one (see KotlinSmoke.kt).
  classpath = sourceSets.main.get().runtimeClasspath
  modularity.inferModulePath.set(false)
  mainClass.set("dev.juherr.mobilityid4j.smoke.KotlinSmoke")
}

tasks.register("smoke") {
  description = "Runs the Java and Kotlin consumer smokes."
  group = "verification"
  dependsOn(tasks.run, runKotlin)
}
