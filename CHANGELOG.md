# Changelog

All notable changes to this repository are documented here, grouped by workspace. The format
follows [Common Changelog](https://common-changelog.org) and versions follow
[Semantic Versioning](https://semver.org). A release is cut by the dispatched `Release` workflow,
which requires a dated section for the version below and a matching file in
`.github/release-notes/`.

## [Unreleased]

### Added

- **Scala:** quality gates: Scalafmt (`.scalafmt.conf`), Scalafix (`OrganizeImports`,
  `RemoveUnused`), fatal compiler warnings on both Scala versions, license headers on test
  sources too, and MiMa binary-compatibility checks of `core` and `interpolators` against the
  last release on Maven Central (skipped until the first one).
- **Release:** the Maven Central jobs (Java and Scala) distinguish a version that is fully
  visible (skipped), never published (uploaded), partially visible (a previous upload still
  propagating: waited for, never uploaded again) or unknown (refused), and wait up to two
  hours for repo1 to serve a release instead of thirty minutes (`scripts/wait-central-release.sh`).
- **Release:** the `Release` workflow now also publishes Scala to Maven Central: a `Preflight
  Scala` job runs `scala/scripts/verify.sh` (full gate on Scala 2.13 and 3, release guard
  wiring proof, consumer smoke from an isolated repository), then `Release Scala` stages both
  Scala versions with `publishSigned` and releases the bundle with `sonaRelease`, idempotently.

- **TypeScript:** `tryParse` on every identifier and in `MobilityIdParsers`, returning a
  `ParseResult<T>` (`{ ok: true, value }` or `{ ok: false, error }`) so callers get the failure
  reason without exceptions.
- **TypeScript:** `ValidationError`, thrown by every strict factory and parser on invalid input.
  It extends `TypeError`, so existing `instanceof TypeError` checks still hold.

- **Release:** the `Release` workflow now also publishes PHP: a `Preflight PHP` job runs
  `composer check`, then `Release PHP` pushes a `git subtree split` of `php/` to the
  `juherr/mobility-id-php` mirror as `vX.Y.Z`, the repository Packagist follows (Packagist
  cannot index a package in a sub-directory). The package ships `LICENSE` and `NOTICE`, and
  `composer.json` carries keywords, homepage and support links.
- **PHP:** 400 cross-language check-digit fixtures (`tests/fixtures/check-digit-{iso,din}.csv`,
  computed by the TypeScript port and verified by the Go port) and unit tests for the ISO
  check-digit matrix arithmetic.
- **Go:** typed sentinel errors (`ErrInvalidCountryCode`, `ErrInvalidContractID`,
  `ErrInvalidCheckDigit`, `ErrInvalidEvseID`, `ErrUnconvertibleContractID`, ...) wrapped with
  `%w` by every constructor, parser and conversion, so callers use `errors.Is`; a composite
  identifier also wraps the failing component's sentinel.
- **Go:** `PartyID.CountryCode()` and `PartyID.PartyCode()` accessors.
- **Go:** testable `Example*` functions for the main entry points (shown on pkg.go.dev), `Fuzz*`
  targets for the parsers, the `FromParts` builders and the check digits, and the 400
  cross-language check-digit fixtures in `mobilityid/testdata/`.

### Fixed

- **TypeScript:** `CountryCode` accepted 30 CLDR region codes that ISO 3166-1 does not assign
  (`EU`, `UK`, `XK`, `SU`, `YU`, ...) because it validated through `Intl.DisplayNames`, whose
  answer also varied with the ICU data of the running Node. The list is now generated from the
  JDK's `Locale.getISOCountries()`, the reference source, like Go.
- **PHP:** `CountryCode` accepted `XK`, a user-assigned code `league/iso3166` ships beyond
  ISO 3166-1; it is now rejected like in the other ports.
- **Go:** `CountryCode` accepted 36 CLDR region codes that ISO 3166-1 does not assign (`UK`,
  `EU`, `XK`, `AN`, `SU`, `DD`, `YU`, ...), diverging from Scala and Java. The
  list is now generated from the JDK's `Locale.getISOCountries()`, the reference source.
  Values that `go/v0.1.0` accepted are therefore rejected (observable change, weighed in #70).
- **Go:** `NewEvseIDISOFromParts` and `NewEvseIDFromParts` no longer drop a leading `E` from
  the power outlet id; as in Scala, Java and TypeScript, `("NL", "TNM", "E840*6487")` keeps
  `PowerOutletID() == "E840*6487"` and renders `NL*TNM*EE840*6487` (the first `E` is the ISO
  id type, added by the renderer).
- **Go:** `NewEvseID` keeps both parser causes when a value is neither ISO nor DIN
  (`invalid EVSE id: 'ZZ*TNM*E840*6487': ISO: invalid ISO 3166-1 alpha-2 country code: 'ZZ';
  DIN: does not match the DIN format`), so `errors.Is` on the component sentinel holds through
  the generic constructor and `EvseID.UnmarshalText`.
- **Go:** `CalculateDIN7064ModXY` returned a negative "digit" for payloads longer than the
  11-character DIN contract id (integer overflow of the power-of-two weights); the sum is now
  reduced modulo 11 at every step. Found by the new fuzz target.
- **Go:** component errors inside `NewEvseIDISO` and `NewEvseIDDIN` (unknown country, invalid
  operator) are now wrapped as EVSE id errors instead of being returned bare.
- **PHP:** `PartyId::of()` now has a real test for the rejection of a DIN operator id longer
  than three digits, replacing an `assertTrue(true)` placeholder.
- **TypeScript:** `EvseIdIso.fromParts` and `EvseId.fromParts` no longer drop a leading `E` from
  the power outlet id; as in Scala and Java, `("NL", "TNM", "E840*6487")` keeps
  `powerOutletId === "E840*6487"` and renders `NL*TNM*EE840*6487`. Found by the new
  render/parse round-trip property.

### Changed

- **TypeScript (breaking):** `CountryCode` is a literal union of the 249 ISO 3166-1 alpha-2
  codes (exported as `ISO_3166_ALPHA2`) instead of a branded string: `const cc: CountryCode =
  "NL"` now type-checks, `"nl"` no longer does (`CountryCode.from("nl")` still normalizes it).
  The companion API is unchanged; the other identifiers stay branded.
- **TypeScript:** toolchain updated to Vite+ 0.3.3 (Oxlint 1.83, Oxfmt 0.68, Vite 8.3,
  Rolldown 1.2.9, tsdown 0.23; Vitest stays 4.1.11) and Bun 1.4.2 (`packageManager`;
  `engines.bun` unchanged). `@vitest/coverage-v8` stays on the Vitest version Vite+ bundles and
  is excluded from Renovate. TypeScript 6 remains the supported baseline; CI adds a TypeScript 7
  compatibility job (type-check, build, declarations, package verification).
- **Breaking (Scala):** the artifacts move to `dev.juherr.mobilityid:mobilityid` and
  `dev.juherr.mobilityid:mobilityid-interpolators` (the `com.thenewmotion` coordinates were
  never on Maven Central; the package stays `com.thenewmotion.mobilityid`). Scala 2.12 and
  3.3 are dropped: the library is built for Scala 2.13.18 and the 3.9.0 LTS, so the Scala 3
  artifact requires a 3.9+ compiler, and the bytecode baseline is JDK 17 (was 1.8).
- **Scala:** sbt 2.0.9 (`mise.toml` `sbt = "2.0"`), specs2 4.23.0, sbt-header 5.11.0; the
  version comes from the release workflow (`version.sbt` removed); `scala/README.md` records
  the tooling decisions and `scala/AGENTS.md` the sbt 2 commands.
- **Breaking (Go):** Go 1.26 is the minimum version (`go 1.26.0` in `go.mod`, CI on 1.26 and
  1.27); Go 1.25 is no longer supported. Error messages now start with the sentinel text
  (`invalid contract id: 'NL': too short`).
- **Breaking (Go):** every identifier implements `encoding.TextMarshaler`, so `encoding/json`
  (and any text codec) now writes it as its canonical string (`"NL-TNM-000122045-U"`) where it
  used to write an empty object (`{}`, the fields are unexported); a zero value fails to
  marshal instead of producing `{}`. `UnmarshalText` is added on every type whose format is
  unambiguous (`ContractID` is decoded through `NewContractID` with the expected standard).
- **Go:** the module has no third-party dependency any more (`golang.org/x/text` dropped).
- **Go:** `.golangci.yml` rewritten for the golangci-lint v2 schema with `gofumpt` and `goimports`
  as formatters and `errorlint`, `gocritic`, `copyloopvar`, `misspell` enabled;
  the license-header gate now really runs (the v1-layout settings were silently ignored). CI
  uses `golangci-lint-action` (2.13, tracked by Renovate), `go test -race -cover` and
  `govulncheck`.
- **Breaking (TypeScript):** `CountryCode`, `PhoneCountryCode`, `ProviderId`, `OperatorIdIso` and
  `OperatorIdDin` are branded strings instead of wrapper classes: `CountryCode.from("nl")` returns
  the plain string `"NL"` typed as `CountryCode`, so `===`, `JSON.stringify`, template literals and
  `Map` keys work on the value itself. `from`, `parse`, `tryParse` and `isValid` keep their
  signatures; the `.value` property and `instanceof` checks are gone. Replace `id.value` with `id`.
  There is no runtime check that a string is branded: where a `CountryCode` is required, parse
  the input (`CountryCode.parse`/`tryParse`) and use the returned value; `isValid` only answers
  whether `from` would accept the input and does not narrow it. `ContractId.fromParts` takes
  plain strings, a branded value being one.
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
