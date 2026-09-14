## mobilityid4j

Java 21 port of the mobility ID domain model and validation logic.

## Layout

- single-module Gradle project, JPMS module `dev.juherr.mobilityid4j` (`src/main/java/module-info.java`)
- sources in `src/main/java`, tests in `src/test/java` (white-box tests patched into the module by Gradle)
- dependency and plugin versions in `gradle/libs.versions.toml` (Gradle version catalog); the two
  settings plugins that bootstrap the build (foojay toolchain resolver, nmcp) keep their version
  inline in `settings.gradle.kts` because the catalog is not available there
- `consumer-smoke/`: a separate Gradle build that consumes the published artifact from an
  isolated repository, from Java on the module path and from Kotlin on the classpath
  (`scripts/verify-consumer.sh`); its Kotlin Gradle plugin version is inline in its own
  `build.gradle.kts`, outside the main catalog
- `scripts/verify.sh`: single verification entry point (gates, release guard wiring, consumer smoke)
- Gradle runs with the configuration cache and build cache enabled (`gradle.properties`)

## Build and test

Run from `java/`:

```bash
./gradlew build
./gradlew test
./gradlew test --tests "*ContractIdTest"
```

Release-oriented checks:

```bash
./gradlew check
./gradlew javadocJar sourcesJar
./gradlew publishToMavenLocal
```

## Formatting and static analysis

The project is configured with:

- Spotless + `palantir-java-format`
- Error Prone
- NullAway (strict, JSpecify mode)
- JSpecify as an `api` dependency (annotations are part of the public API and have runtime
  retention), `@NullMarked` module and packages, `requires static transitive org.jspecify`
- JaCoCo with coverage gates (90 % lines, 80 % branches) in `check`
- japicmp against the last release on Maven Central (`-PapiBaselineVersion=X.Y.Z` to pin; skipped
  with an explicit warning while nothing is published)
- `javac -Xlint:all -Werror`
- `javadoc -Xdoclint:all -Werror`
- reproducible jars (no timestamps, stable entry order)

Useful commands:

```bash
./gradlew spotlessApply
./gradlew spotlessCheck
```

## Tests

- JUnit Jupiter + AssertJ for example-based suites (`*Test`); `ParseInvalidInputTest` and
  `MobilityIdParsersInvalidInputTest` pin the strict/tolerant contract for every parser family
  (invalid input: strict throws, tolerant returns `null`).
- jqwik for property-based suites (`*PropertyTest`): check-digit invariants (alphabet, case
  insensitivity, single-substitution detection for ISO) and contract-id round trips/conversions
  over generated inputs. jqwik runs on the JUnit Platform, no extra wiring.

## API choices

- idiomatic Java API (not a 1:1 Scala mirror)
- two entry points per identifier: strict factories/parsers (`of`, `parseStrict`) throw
  `IllegalArgumentException` on invalid input; tolerant parsers (`parse`, `MobilityIdParsers.*`)
  return `@Nullable T` (JSpecify) and never throw, never `Optional`
- immutable domain types
- local `var` is preferred when the inferred type is obvious at a glance; otherwise explicit types are kept for readability

## Usage examples

Parse contract ID (tolerant API):

```java
var contract = ContractId.parse(ContractIdStandard.ISO, "NL-TNM-000722345-X");
if (contract != null) {
  System.out.println(contract.toCompactString());
}
```

Create contract ID (strict API):

```java
var contract = ContractId.of(ContractIdStandard.ISO, "NL", "TNM", "000722345");
System.out.println(contract); // NL-TNM-000722345-X
```

Convert contract standard:

```java
var din = ContractId.parseStrict(ContractIdStandard.DIN, "NL-TNM-012204-5");
var emi3 = din.convertTo(ContractIdStandard.EMI3);
var iso = din.convertTo(ContractIdStandard.ISO);
```

Parse EVSE ID and branch by format:

```java
var evse = EvseId.parse("NL*TNM*E840*6487");
if (evse instanceof EvseIdIso isoEvse) {
  System.out.println(isoEvse.toCompactString());
}
```

## Using from Kotlin

The library is annotated for Kotlin interop out of the box:

- Nullability is declared with JSpecify (`@NullMarked` module and packages, `@Nullable` on the
  tolerant parser parameters), so Kotlin sees platform-free types: `String` parameters are
  non-null, `parse(raw: String?)` accepts null. If your Kotlin version does not treat JSpecify as
  strict by default, add `-Xjspecify-annotations=strict` to `freeCompilerArgs`.
- Tolerant parsers return `@Nullable T`, seen as `T?` from Kotlin:
  `ContractId.parse(ContractIdStandard.ISO, raw)?.toCompactString() ?: "invalid"`.
- `EvseId` and `OperatorId` are Java sealed interfaces: `when (evse) { is EvseIdIso -> ...; is EvseIdDin -> ... }`
  is exhaustive without an `else` branch.
- Domain types are Java records: components are accessed as functions (`countryCode.value()`),
  not as properties.
- No checked exceptions: strict factories throw `IllegalArgumentException`, tolerant parsers
  return `null`.
- `consumer-smoke/src/main/kotlin` is compiled with `-Xjspecify-annotations=strict` and
  `-Werror` in CI and only uses tolerant results through `?.` / `null` branches on the call
  expression itself, so a return type that flips to non-null fails the build ("unnecessary safe
  call"), and a strict factory that flips to nullable fails the non-null assignment.

## Publishing (Maven Central Portal)

Publication goes through the [nmcp](https://gradleup.com/nmcp/) settings plugin
(`settings.gradle.kts`), which uploads a signed bundle to the Central Portal publisher API and
publishes it automatically once validated.

Configured publication coordinates:

- `groupId`: `dev.juherr.mobilityid`
- `artifactId`: `mobilityid4j`

Inputs are read from Gradle properties or environment variables:

- `mavenCentralUsername` / `MAVEN_CENTRAL_USERNAME` and `mavenCentralPassword` /
  `MAVEN_CENTRAL_PASSWORD`: a Central Portal user token (CI maps them from the `CENTRAL_USERNAME`
  and `CENTRAL_TOKEN` secrets)
- `signingKey` / `SIGNING_KEY` and `signingPassword` / `SIGNING_PASSWORD`: armored in-memory PGP
  key and passphrase (CI maps them from `GPG_PRIVATE_KEY` and `GPG_PASSPHRASE`)
- `releaseVersion`: the version to publish (`0.1.0-SNAPSHOT` when absent)

Commands:

```bash
./gradlew -PreleaseVersion=X.Y.Z check javadocJar sourcesJar publishToMavenLocal   # dry run
./gradlew -PreleaseVersion=X.Y.Z publishAggregationToCentralPortal                  # upload + publish
```

`verifyRelease` runs before any upload and refuses a `-SNAPSHOT` version or missing signing inputs.

Release procedure (dispatched `Release` workflow, environment `maven-central`): `CONTRIBUTING.md`.
`scripts/verify-release-wiring.sh` checks the guard without publishing: SNAPSHOT rejected,
signing inputs required, `verifyRelease` scheduled before the nmcp upload task.

## Security scanning

- Pull requests run dependency review in `.github/workflows/dependency-review.yml`.
- OWASP Dependency-Check runs in `.github/workflows/security.yml`.
