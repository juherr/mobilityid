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

- **Repository:** `Trusted Dependency Review` workflow (`workflow_run` from `main`): recomputes
  the dependency review of every pull request from the default branch and publishes it as the
  `Trusted dependency review` commit status through a dedicated GitHub App (environment
  `trusted-review`, branch policy `main`), so that no pull request author can forge the required
  check; fork and Dependabot pull requests also get their Java dependency graph submitted by it
  before merge, once the provenance of the uploaded snapshot is validated (bound to the
  triggering run and its pull request).
- **Release:** the `Release` workflow now also publishes PHP: a `Preflight PHP` job runs
  `composer check`, then `Release PHP` pushes a `git subtree split` of `php/` to the
  `juherr/mobility-id-php` mirror as `vX.Y.Z`, the repository Packagist follows (Packagist
  cannot index a package in a sub-directory). The package ships `LICENSE` and `NOTICE`, and
  `composer.json` carries keywords, homepage and support links.
- **PHP:** 400 cross-language check-digit fixtures (`tests/fixtures/check-digit-{iso,din}.csv`,
  computed by the TypeScript port and verified by the Go port) and unit tests for the ISO
  check-digit matrix arithmetic.

### Fixed

- **PHP:** `PartyId::of()` now has a real test for the rejection of a DIN operator id longer
  than three digits, replacing an `assertTrue(true)` placeholder.
- **TypeScript:** `EvseIdIso.fromParts` and `EvseId.fromParts` no longer drop a leading `E` from
  the power outlet id; as in Scala and Java, `("NL", "TNM", "E840*6487")` keeps
  `powerOutletId === "E840*6487"` and renders `NL*TNM*EE840*6487`. Found by the new
  render/parse round-trip property.

### Changed

- **Breaking (PHP):** PHP 8.4 is the minimum version (`php: ^8.4`, CI on 8.4 and 8.5); PHP 8.3
  is no longer supported. Every value object is a `final readonly class` (`AbstractContractId`
  and `AbstractEvseId` are `abstract readonly`), so their public properties can no longer be
  assigned. Nothing had been published on Packagist before this change.
- **PHP:** source headers and `NOTICE` credit Julien Herr only; the `README.md` states the port
  is inspired by the Scala library, as for the TypeScript workspace.
- **PHP:** `composer check` now chains `composer validate --strict`, `composer normalize
  --dry-run`, `composer audit --abandoned=fail`, php-cs-fixer (`@PER-CS2.0` + `@PhpCsFixer`),
  PHPStan level 10 with `phpstan-strict-rules`, `phpstan-deprecation-rules` and
  `phpstan-phpunit` (the `Locale::getISOCountries()` ignore is gone), a Rector dry run
  (`rector.php`, PHP 8.4 level set) and Infection with a 95 % minimum MSI (currently 100 %).
  Dependencies updated: php-cs-fixer 3.95, PHPStan 2.2, PHPUnit 13.3, league/iso3166 4.5.
- **PHP:** `CountryCode` validates through `ISO3166::alpha2()` instead of caching the whole
  country table; the check-digit classes no longer keep lazily initialised static state.
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
