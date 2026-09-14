# AGENTS.md (java workspace)

Java 21 port `mobilityid4j` (package `dev.juherr.mobilityid4j`). Shared guidance (domain model,
parity rule, license header text, release tags) is in the root `AGENTS.md`; API choices and usage
examples are in `README.md`. Run every command below from `java/`.

## Layout

- `src/main/java/dev/juherr/mobilityid4j` -> domain types and check-digit algorithms.
- `src/main/java/dev/juherr/mobilityid4j/interpolators` -> `MobilityIdParsers` helper API.
- `src/main/java/module-info.java` -> JPMS module `dev.juherr.mobilityid4j`; export any new package there.
- `src/test/java/...` -> JUnit 6 suites named `*Test`, jqwik property suites named `*PropertyTest`.
- `gradle/libs.versions.toml` -> every dependency and plugin version (Renovate updates it); never inline a version in `build.gradle.kts`.
- `bin/` is IDE output (Eclipse/VS Code); never commit it.

## Toolchain

- Gradle wrapper (`./gradlew`, SHA pinned in `gradle/wrapper/gradle-wrapper.properties`),
  Java 21 toolchain (auto-provisioned via the foojay resolver), configuration cache + build cache on; CI tests on JDK 21 and 25 (`.github/workflows/ci-java.yml`).
- Spotless with palantir-java-format + Apache license header, Error Prone + NullAway + JSpecify,
  `javac -Xlint:all -Werror` (minus `exports`), `javadoc -Xdoclint:all -Werror`, reproducible jars.
  `check` runs all of them; a new warning is a build failure.
- Publishing: nmcp settings plugin (`settings.gradle.kts`) uploads to the Maven Central Portal;
  POM/signing stay in `build.gradle.kts`; version comes from `-PreleaseVersion` (defaults to `0.1.0-SNAPSHOT`).

## Commands

- Build / test: `./gradlew build`, `./gradlew test`.
- One suite: `./gradlew test --tests "*ContractIdTest"`.
- Format + headers: `./gradlew spotlessApply`; verify: `./gradlew spotlessCheck`.
- Release readiness: `./gradlew check`, `./gradlew javadocJar sourcesJar`, `./gradlew publishToMavenLocal`.
- Publish (CI only): `./gradlew -PreleaseVersion=X.Y.Z publishAggregationToCentralPortal`; `verifyRelease` blocks SNAPSHOTs and unsigned uploads.

## Code Style

- Keep APIs idiomatic Java; prefer local `var` only when the inferred type is obvious at a glance.
- Mirror the Scala shape: strict factories throw `IllegalArgumentException`, forgiving parsers
  return `Optional`/`null` as documented in `README.md`, immutable value types, canonical `toString()`.
- Formatting is not negotiable: run `spotlessApply` before finishing; NullAway errors are build failures.
- Any new task or plugin must stay configuration-cache compatible (`dependencyCheckAnalyze` is the only opt-out).
- Add a jqwik property when a behavior holds for a whole input class (round trips, algorithm invariants), an example test for known vectors.
