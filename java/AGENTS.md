# AGENTS.md (java workspace)

Java 21 port `mobilityid4j` (package `dev.juherr.mobilityid4j`). Shared guidance (domain model,
parity rule, license header text, release tags) is in the root `AGENTS.md`; API choices and usage
examples are in `README.md`. Run every command below from `java/`.

## Layout

- `src/main/java/dev/juherr/mobilityid4j` -> domain types and check-digit algorithms.
- `src/main/java/dev/juherr/mobilityid4j/interpolators` -> `MobilityIdParsers` helper API.
- `src/test/java/...` -> JUnit 6 suites named `*Test` (`ContractIdTest`, `ParseNullHandlingTest`, ...).
- `bin/` is IDE output (Eclipse/VS Code); never commit it.

## Toolchain

- Gradle wrapper (`./gradlew`, SHA pinned in `gradle/wrapper/gradle-wrapper.properties`),
  Java 21 toolchain; CI tests on JDK 21 and 25 (`.github/workflows/ci-java.yml`).
- Spotless with palantir-java-format 2.87.0 (Java 25 compatible) + Apache license header,
  Error Prone + NullAway + JSpecify annotations. `check` runs all of them.
- Publishing metadata/signing for Maven Central Portal is in `build.gradle.kts`; version comes from
  `-PreleaseVersion` (defaults to `0.1.0-SNAPSHOT`).

## Commands

- Build / test: `./gradlew build`, `./gradlew test`.
- One suite: `./gradlew test --tests "*ContractIdTest"`.
- Format + headers: `./gradlew spotlessApply`; verify: `./gradlew spotlessCheck`.
- Release readiness: `./gradlew check`, `./gradlew javadocJar sourcesJar`, `./gradlew publishToMavenLocal`.

## Code Style

- Keep APIs idiomatic Java; prefer local `var` only when the inferred type is obvious at a glance.
- Mirror the Scala shape: strict factories throw `IllegalArgumentException`, forgiving parsers
  return `Optional`/`null` as documented in `README.md`, immutable value types, canonical `toString()`.
- Formatting is not negotiable: run `spotlessApply` before finishing; NullAway errors are build failures.
