# Changelog

All notable changes to this repository are documented here, grouped by workspace. The format
follows [Common Changelog](https://common-changelog.org) and versions follow
[Semantic Versioning](https://semver.org). A release is cut by the dispatched `Release` workflow,
which requires a dated section for the version below and a matching file in
`.github/release-notes/`.

## [Unreleased]

### Changed

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
