# juherr/mobility-id

PHP port of the Mobility ID domain library, inspired by the original Scala library (`scala/` in
this repository, created by The New Motion): same domain model, same identifiers, same test
fixtures, written for PHP 8.4+.

## Goals

- Keep behavior aligned with the Scala implementation (the behavior reference of the monorepo).
- Provide an idiomatic PHP API with strict and forgiving parsing entry points.
- Enforce quality gates from day one: formatting, license headers, static analysis, automated
  refactoring, unit and mutation tests.

## Chosen Architecture

- One Composer package under `php/`, mirrored to a split repository for Packagist (see
  "Publishing").
- Namespace `Juherr\MobilityId` (`src/`), tests in `Juherr\MobilityId\Tests` (`tests/`), PSR-4.
- Domain model + validation + check-digit algorithms + parsing helpers, no framework dependency.
  `league/iso3166` provides the ISO 3166-1 alpha-2 table (it needs `ext-mbstring`); no other
  PHP extension is required.

## Tooling Decisions

- **PHP baseline: 8.4** (`composer.json` `php: ^8.4.1`, `config.platform.php = 8.4.1` (PHPUnit 13 needs 8.4.1), CI on 8.4
  and 8.5). The code uses `final readonly class` / `abstract readonly class` (8.2), typed class
  constants (8.3) and `new Foo()->method()` without wrapping parentheses (8.4). The baseline was
  raised from 8.3 to track the currently supported PHP releases and keep the Rector level set at
  the latest stable version. Property hooks and asymmetric visibility are not used: promoted
  `public` properties on a `readonly` class already give immutable value objects without
  accessors.
- **Build/Dependencies**: Composer. `composer.json` is kept normalized (`composer normalize`)
  and validated with `--strict`; `composer audit --abandoned=fail` runs in the gate.
- **Tests**: PHPUnit 13 (attributes, data providers). `tests/fixtures/check-digit-{iso,din}.csv`
  hold 200 payloads each whose check digits were computed by the TypeScript and Go ports (which
  agree on every row), so the PHP algorithms are pinned to the other workspaces beyond the Scala
  fixtures.
- **Mutation testing**: Infection (`infection.json5`), minimum MSI and covered MSI of 95 % on
  `src/`; the current score is 100 %. `PublicVisibility` is disabled (a library's public
  methods are its API). It needs a coverage driver (`pcov` in CI).
- **Formatting**: php-cs-fixer with `@PER-CS2.0` + `@PhpCsFixer` (and their `:risky` sets),
  `@PHP84Migration`, non-Yoda comparisons, `self::assert*()` in tests; also writes the license
  header (`header_comment`). `php_unit_strict` is off because the tests compare value objects
  with `assertEquals` on purpose.
- **Static analysis**: PHPStan level 10 with `bleedingEdge`, `phpstan-strict-rules`,
  `phpstan-deprecation-rules` and `phpstan-phpunit` (auto-loaded by `extension-installer`), on
  `src/` and `tests/`, no ignore list.
- **Refactoring**: Rector (`rector.php`) with the PHP level set resolved from `composer.json`,
  dead code, code quality, type declarations, early return and PHPUnit sets (no privatization
  set: nothing may narrow the public API automatically).
  `rector:check` (dry run) is part of the gate, so any drift fails CI. Two rules are skipped:
  `PreferPHPUnitThisCallRector` (conflicts with php-cs-fixer's static assertions) and
  `YieldDataProviderRector` (detaches trailing comments from data-provider rows).
- **CI matrix**: PHP 8.4 and 8.5 (`.github/workflows/ci-php.yml`), `coverage: pcov`.

## API Design Decisions

- PHP API is idiomatic, not a 1:1 Scala mirror.
- Strict factories (`of(...)`, `ofParts(...)`) throw `InvalidArgumentException`; forgiving
  parsers (`opt(...)`, `parse(...)`) return `?T`.
- Domain types are immutable: every value object is a `final readonly class` (the two shared
  bases are `abstract readonly class`), so their public properties are read-only.
- The contract-id and EVSE-id standards are marker interfaces (`ContractIdStandard\Iso`,
  `ContractIdStandard\Emi3`, `ContractIdStandard\Din`, `EvseIdStandard\Iso`,
  `EvseIdStandard\Din`) and one final class per standard; there is no string constant to replace
  with a backed enum, dispatch on the standard is done with `instanceof`.
- Input is uppercased once, at the factory; comparisons and check digits work on the normalized
  value. Canonical rendering is preserved (`__toString()`, `toString()`, `toCompactString()`).
- `PartyId::of()` accepts `CountryCode|PhoneCountryCode` and
  `ProviderId|OperatorIdIso|OperatorIdDin` (wider than Scala, which only pairs `CountryCode`
  with `ProviderId`/`OperatorIdIso`); a DIN operator id longer than three digits is rejected
  because it is not a party code.
- Check-digit algorithms are pure static functions without cached state: the tables are cheap to
  rebuild and a lazily initialised static cache would only be exercised by the first test of a
  process, hiding table mutations from Infection.

## Core Components

### Foundational Identifiers
- `CountryCode`: ISO 3166-1 alpha-2 country codes (e.g., "NL", "DE"), validated with
  `league/iso3166`.
- `PhoneCountryCode`: Phone country codes (e.g., "+31", "+49").
- `ProviderId`: Three-character identifier for providers (e.g., "TNM", "ABC").
- `OperatorIdIso`: ISO-style operator ID (three alphanumeric characters).
- `OperatorIdDin`: DIN-style operator ID (three to six digits).
- `PartyId`: Combination of CountryCode and Provider/Operator ID (e.g., "NL-TNM").

### Check-Digit Algorithms
- `CheckDigitIso`: ISO 15118-1 check digit (also used by EMI3).
- `CheckDigitDin`: DIN SPEC 91286 check digit.

### Contract Model
- `AbstractContractId`: Base class for contract IDs, with the `convertToDin()`,
  `convertToEmi3()` and `convertToIso()` conversions.
- `ContractIdIso`, `ContractIdEmi3`, `ContractIdDin`: one final class per standard.
- `ContractIdParser`: regexes and check-digit helpers shared by the three classes.

### EVSE Model
- `AbstractEvseId`: Base class for EVSE IDs.
- `EvseIdIso`, `EvseIdDin`: one final class per format.
- `EvseIdParser`: regexes shared by the two classes.
- `EvseId`: entry point that tries ISO first, then DIN.

## Commands

All commands run from `php/`.

```bash
composer install
composer check          # full gate, what CI runs (see below)
composer test           # PHPUnit
composer test:coverage  # PHPUnit with a text coverage report (needs pcov or xdebug)
composer analyse        # PHPStan
composer format         # php-cs-fixer, applies formatting and license headers
composer format:check   # php-cs-fixer, dry run
composer rector         # Rector, applies refactorings
composer rector:check   # Rector, dry run
composer infection      # mutation testing (needs pcov or xdebug)
./vendor/bin/phpunit --filter ContractIdIsoTest
```

`composer check` chains `composer validate --strict`, `composer normalize --dry-run`,
`composer audit --abandoned=fail`, `format:check`, `analyse`, `rector:check`, `test` and
`infection`.

### Local PHP 8.4 with pcov

`mise.toml` pins PHP 8.4 (Homebrew `shivammathur/php` behind the mise plugin; run
`brew trust shivammathur/php` once if mise cannot list versions). Infection needs a coverage
driver; `pecl install pcov` fails to find `pcre2.h` with Homebrew PHP, pass the include path:

```bash
CPPFLAGS="-I$(brew --prefix pcre2)/include" pecl install pcov
```

## Publishing

Packagist only indexes a repository whose `composer.json` sits at its root, so `php/` is
published through a read-only split repository (`juherr/mobility-id-php`) that Packagist
follows. PHP is part of the common `Release` workflow (`.github/workflows/release.yml`, one
`vX.Y.Z` version for every port): the `Preflight PHP` job runs `composer check` and checks the
deploy key is present before any registry is touched, then `Release PHP` splits the `php/`
history with `git subtree split` and pushes the split commit to the mirror's `main` and as the
`vX.Y.Z` tag (refusing to move an existing tag). The mirror is written with a deploy key stored
as `PHP_MIRROR_DEPLOY_KEY` in the `packagist` environment. One-time setup and the release
procedure are in `CONTRIBUTING.md`.

Install from Packagist:

```bash
composer require juherr/mobility-id
```
