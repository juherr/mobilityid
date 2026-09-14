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
  `GPG_PASSPHRASE`, `NPM_TOKEN`.

### Added

- **Java:** jqwik property-based suites for check digits and contract-id round trips; JaCoCo
  coverage gates (90 % lines, 80 % branches); japicmp API-compatibility check against the last
  published release; isolated Java + Kotlin consumer smoke on the module path
  (`java/scripts/verify-consumer.sh`); `java/scripts/verify.sh` as the single verification entry
  point.
- **Repository:** workflow linting (actionlint, zizmor), `CONTRIBUTING.md`, this changelog.
