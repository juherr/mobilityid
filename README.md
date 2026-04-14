## mobilityid monorepo

[![CI](https://github.com/juherr/mobilityid/actions/workflows/ci.yml/badge.svg)](https://github.com/juherr/mobilityid/actions/workflows/ci.yml)

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
bun run check
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
- `mise.toml` uses simplified version formats: major.minor for sbt/gradle (e.g., `1.12`, `8.9`), major only for Java/Node (e.g., `21`, `24`). See `AGENTS.md` for configuration details.
- CI validates Gradle Wrapper integrity on every run (`gradle/actions/wrapper-validation`).
- Gradle distribution integrity is pinned with `distributionSha256Sum` in `java/gradle/wrapper/gradle-wrapper.properties`.
- Pull requests run a dependency review gate (`actions/dependency-review-action`) via `.github/workflows/ci.yml`.
- OWASP dependency scanning runs in `.github/workflows/security.yml`.

## Java release checks

Run from `java/`:

```bash
./gradlew check
./gradlew javadocJar sourcesJar
./gradlew publishToMavenLocal
```

`mobilityid4j` publishing metadata/signing is configured for Maven Central Portal workflows.

## Global release tags

- Global tags `vX.Y.Z` trigger the release pipeline (`.github/workflows/release.yml`).
- The Java publication uses the tag version and publishes `mobilityid4j` to Maven Central Portal.
- The TypeScript publication uses the same tag version and publishes `@juherr/mobilityid` to npm.

## License

- Repository-level license: `LICENSE`
- Original Scala license file preserved at: `scala/LICENSE`

## Acknowledgements

Many thanks to the original Mobility ID Utils authors and contributors at The New Motion (now Shell Recharge Solutions EU) for creating and open-sourcing the Scala implementation that this repository builds upon.
