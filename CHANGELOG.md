# Changelog

All notable changes to this repository are documented here, grouped by workspace. The format
follows [Common Changelog](https://common-changelog.org) and versions follow
[Semantic Versioning](https://semver.org). A release is cut by the dispatched `Release` workflow,
which requires a dated section for the version below and a matching file in
`.github/release-notes/`.

## [Unreleased]

### Added

- **TypeScript:** `tryParse` on every identifier and in `MobilityIdParsers`, returning a
  `ParseResult<T>` (`{ ok: true, value }` or `{ ok: false, error }`) so callers get the failure
  reason without exceptions.
- **TypeScript:** `ValidationError`, thrown by every strict factory and parser on invalid input.
  It extends `TypeError`, so existing `instanceof TypeError` checks still hold.

### Fixed

- **TypeScript:** `EvseIdIso.fromParts` and `EvseId.fromParts` no longer drop a leading `E` from
  the power outlet id; as in Scala and Java, `("NL", "TNM", "E840*6487")` keeps
  `powerOutletId === "E840*6487"` and renders `NL*TNM*EE840*6487`. Found by the new
  render/parse round-trip property.

### Changed

- **TypeScript:** source headers, `NOTICE` and the header gate credit Julien Herr only; the
  `README.md` states the port is inspired by the Scala library. The package now ships the full
  Apache 2.0 text as `LICENSE` plus a `NOTICE` file, and the release package check fails when
  `LICENSE` is only the short header or `NOTICE` lacks the copyright line.
- **Repository:** root `LICENSE` is the full Apache 2.0 text; the workspace list and which
  copyright notice applies to each move to `NOTICE`.
- **TypeScript:** `parse` is now derived from `tryParse`: it returns `null` for a
  `ValidationError` only, so an unexpected exception inside a parser (a bug) propagates instead
  of being reported as an invalid input. `EvseId.parseStrict` and `EvseId.tryParse` report both
  reasons when a value is neither ISO nor DIN (`Invalid EVSE ID: … (ISO: …; DIN: …)`), where
  `parseStrict` used to say only `Invalid EVSE ID: …`.
- **TypeScript:** ESM-only package resolved through `exports` alone (the legacy `main`/`types`
  fields are gone; Node >= 22 was already required); the published shape is checked by
  `publint --strict` and Are The Types Wrong; stricter `tsconfig` (`verbatimModuleSyntax`,
  `isolatedDeclarations`, `erasableSyntaxOnly`, ES2023) with an explicit TypeScript 6
  devDependency; property-based tests (fast-check) and coverage thresholds; Vite+ 0.3.
- **CI:** OWASP Dependency-Check no longer runs on pull requests; it scans `main` weekly and on
  demand with a cached NVD database and fails on CVSS >= 7.0, aligned with the pull request
  dependency review gate.
- **CI:** the resolved Gradle dependency graph (transitive dependencies and build plugins
  included) is now submitted to GitHub for `main` and same-repository pull requests, so
  Dependency Review and Dependabot alerts finally cover the Java workspace; Dependency Review now
  fails on high/critical vulnerabilities in every scope, development dependencies included.
- **Breaking (Java):** tolerant `parse*` methods return `@Nullable T` instead of `Optional<T>`
  so Kotlin callers get `T?` and the shape matches the other ports; wrap with
  `Optional.ofNullable(...)` when an `Optional` is wanted. Nothing had been published before this
  change.
- **Java:** Gradle 9.7.1 with version catalog, configuration cache and build cache; `javac -Xlint:all
  -Werror` and `javadoc -Xdoclint:all -Werror`; reproducible jars; JSpecify exposed as an `api`
  dependency with a `@NullMarked` JPMS module `dev.juherr.mobilityid4j`.
- **Java:** publication goes through the nmcp Central Portal plugin, guarded by `verifyRelease`.
- **Release:** the `Release` workflow is dispatched manually with the version as input, validates
  the changelog and release notes, publishes idempotently, then creates the signed `vX.Y.Z` tag
  and the GitHub Release; secrets are `CENTRAL_USERNAME`, `CENTRAL_TOKEN`, `GPG_PRIVATE_KEY`,
  `GPG_PASSPHRASE`; npm is published through Trusted Publishing (OIDC) with no token.
- **TypeScript:** package metadata completed for publication (`repository`, `homepage`, `bugs`,
  `keywords`, `publishConfig`, `sideEffects`, bundled `LICENSE`); `scripts/verify-package.sh`
  packs, checks (content, publint) and consumes the tarball from a throw-away Node + TypeScript
  project, in CI and in the release preflight.

### Added

- **Java:** jqwik property-based suites for check digits and contract-id round trips; JaCoCo
  coverage gates (90 % lines, 80 % branches); japicmp API-compatibility check against the last
  published release; isolated consumer smoke consuming the published artifact from Java on the
  module path and from Kotlin (classpath, strict JSpecify) (`java/scripts/verify-consumer.sh`); `java/scripts/verify.sh` as the single verification entry
  point.
- **Repository:** workflow linting (actionlint, zizmor), Codecov diff coverage on pull requests
  (Java flag first), tested release scripts (`scripts/tests`: version validation, Central resolution check, npm tarball content), `CONTRIBUTING.md`, this changelog.
