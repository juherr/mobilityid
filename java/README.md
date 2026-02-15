## mobilityid4j

Java 21 port of the mobility ID domain model and validation logic.

## Layout

- single-module Gradle project
- sources in `src/main/java`
- tests in `src/test/java`

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
- NullAway (strict)
- JSpecify (`@NullMarked` packages)

Useful commands:

```bash
./gradlew spotlessApply
./gradlew spotlessCheck
```

## API choices

- idiomatic Java API (not a 1:1 Scala mirror)
- tolerant parsing methods return `Optional<T>`
- strict factory methods throw `IllegalArgumentException`
- immutable domain types
- local `var` is preferred when the inferred type is obvious at a glance; otherwise explicit types are kept for readability

## Usage examples

Parse contract ID (tolerant API):

```java
var maybeContract = ContractId.parse(ContractIdStandard.ISO, "NL-TNM-000722345-X");
if (maybeContract.isPresent()) {
  var contract = maybeContract.orElseThrow();
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
var maybeEvse = EvseId.parse("NL*TNM*E840*6487");
if (maybeEvse.isPresent()) {
  var evse = maybeEvse.orElseThrow();
  if (evse instanceof EvseIdIso isoEvse) {
    System.out.println(isoEvse.toCompactString());
  }
}
```

## Publishing (Maven Central Portal)

The build is ready for publication metadata/signing and local dry runs.

Configured publication coordinates:

- `groupId`: `dev.juherr.mobilityid`
- `artifactId`: `mobilityid4j`

Publishing credentials are read from Gradle properties or environment variables:

- `mavenCentralPortalUrl` / `MAVEN_CENTRAL_PORTAL_URL`
- `mavenCentralUsername` / `MAVEN_CENTRAL_USERNAME`
- `mavenCentralPassword` / `MAVEN_CENTRAL_PASSWORD`

Signing keys are read from:

- `signingKey` / `SIGNING_KEY`
- `signingPassword` / `SIGNING_PASSWORD`

Global release tags:

- A global repository tag `vX.Y.Z` triggers `.github/workflows/release.yml`.
- The Java release job derives `X.Y.Z` from the tag and publishes `mobilityid4j` with that version.

## Security scanning

- Pull requests run dependency review in `.github/workflows/ci.yml`.
- OWASP Dependency-Check runs in `.github/workflows/security.yml`.
