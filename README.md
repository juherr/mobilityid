## mobilityid monorepo

[![CI Java](https://github.com/juherr/mobilityid/actions/workflows/ci-java.yml/badge.svg)](https://github.com/juherr/mobilityid/actions/workflows/ci-java.yml)
[![CI Scala](https://github.com/juherr/mobilityid/actions/workflows/ci-scala.yml/badge.svg)](https://github.com/juherr/mobilityid/actions/workflows/ci-scala.yml)
[![CI Go](https://github.com/juherr/mobilityid/actions/workflows/ci-go.yml/badge.svg)](https://github.com/juherr/mobilityid/actions/workflows/ci-go.yml)
[![CI PHP](https://github.com/juherr/mobilityid/actions/workflows/ci-php.yml/badge.svg)](https://github.com/juherr/mobilityid/actions/workflows/ci-php.yml)
[![CI TypeScript](https://github.com/juherr/mobilityid/actions/workflows/ci-ts.yml/badge.svg)](https://github.com/juherr/mobilityid/actions/workflows/ci-ts.yml)
[![codecov](https://codecov.io/gh/juherr/mobilityid/graph/badge.svg)](https://codecov.io/gh/juherr/mobilityid)

This repository contains multiple implementations of the same mobility ID domain:

- `scala/`: original Scala implementation (sbt + specs2)
- `java/`: Java 21 port (`mobilityid4j`, Gradle)
- `go/`: Go port (`mobilityid.juherr.dev/go`)
- `php/`: PHP port (`juherr/mobilityid`)
- `ts/`: TypeScript port (`@juherr/mobilityid`, Bun + Vite+)

## Quick start

### Scala workspace

```bash
cd scala
sbt test
```

Run a single Scala suite:

```bash
cd scala
sbt "core/testOnly com.thenewmotion.mobilityid.ContractIdSpec"
```

### Java workspace

```bash
cd java
./gradlew test
```

Run a single Java suite:

```bash
cd java
./gradlew test --tests "*ContractIdTest"
```

### TypeScript workspace

```bash
cd ts
vp install
vp check
```

Run a single TypeScript test pattern:

```bash
cd ts
vp test ContractId
```

## Documentation

- Scala docs and original usage examples: `scala/README.md`
- Java-specific docs and design choices: `java/README.md`
- Go-specific docs and design choices: `go/README.md`
- PHP-specific docs and design choices: `php/README.md`
- TypeScript-specific docs and design choices: `ts/README.md`

## Dependency updates

- Renovate manages GitHub Actions, Gradle, npm, sbt/Scala, and `mise.toml` tool versions (`.github/renovate.json`).
- `mise.toml` uses simplified version formats: major.minor for sbt/gradle (e.g., `1.12`, `9.7`), major only for Java/Node (e.g., `21`, `24`). See `AGENTS.md` for configuration details.
- CI validates Gradle Wrapper integrity on every run (`gradle/actions/wrapper-validation`).
- Gradle distribution integrity is pinned with `distributionSha256Sum` in `java/gradle/wrapper/gradle-wrapper.properties`.
- Pull requests run a dependency review gate (`actions/dependency-review-action`) via `.github/workflows/dependency-review.yml`.
- OWASP Dependency-Check scans the full Java dependency set weekly on `main` (and on demand) in `.github/workflows/security.yml`; it is not a pull request gate.

## Java release checks

Run from `java/`:

```bash
./gradlew check
./gradlew javadocJar sourcesJar
./gradlew publishToMavenLocal
```

`mobilityid4j` is published to the Maven Central Portal through the nmcp Gradle plugin; details in `java/README.md`.

## Releases

- Java and TypeScript are released together by the manually dispatched `Release` workflow (`.github/workflows/release.yml`): it validates `CHANGELOG.md` and `.github/release-notes/X.Y.Z.md`, publishes `mobilityid4j` to Maven Central and `@juherr/mobilityid` to npm, then creates the signed `vX.Y.Z` tag and the GitHub Release.
- Go is released from `go/vX.Y.Z` tags (`.github/workflows/release-go.yml`).
- Procedure and required secrets: `CONTRIBUTING.md`.

## License

- Repository-level license: `LICENSE`
- Original Scala license file preserved at: `scala/LICENSE`

## Acknowledgements

Many thanks to the original Mobility ID Utils authors and contributors at The New Motion (now Shell Recharge Solutions EU) for creating and open-sourcing the Scala implementation that this repository builds upon.
