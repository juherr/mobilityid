# AGENTS.md (php workspace)

PHP port, Composer package `juherr/mobility-id`, namespace `Juherr\MobilityId` (tests in
`Juherr\MobilityId\Tests`). Shared guidance (domain model, parity rule) is in the root
`AGENTS.md`; architecture, tooling and API decisions are in `README.md`. Run every command below
from `php/`.

## Layout

- `src/` -> one `final readonly class` per identifier or standard (`ContractIdIso`,
  `ContractIdEmi3`, `ContractIdDin`, `EvseIdIso`, `EvseIdDin`, ...), shared bases
  (`AbstractContractId`, `AbstractEvseId`, both `abstract readonly`), `ContractIdParser` /
  `EvseIdParser` helpers, `CheckDigitIso` / `CheckDigitDin` (stateless static functions),
  marker interfaces in `ContractIdStandard/` and `EvseIdStandard/`.
- `tests/` -> PHPUnit suites, one `*Test.php` per class; `tests/fixtures/check-digit-*.csv`
  are cross-language check-digit fixtures (generated with the TypeScript port, verified with the
  Go port) and must not be edited by hand.
- `LICENSE` and `NOTICE` ship with the package (the split repository is the Packagist source).

## Toolchain

- PHP 8.4+ (`mise.toml`); CI runs 8.4 and 8.5 with `pcov` (`.github/workflows/ci-php.yml`).
- php-cs-fixer (`@PER-CS2.0` + `@PhpCsFixer`, license header), PHPStan level 10 + strict rules,
  Rector, PHPUnit 13, Infection (minimum MSI 95 %), composer-normalize.
- Configuration files: `.php-cs-fixer.dist.php`, `phpstan.neon`, `rector.php`,
  `infection.json5`, `phpunit.xml`.

## Commands

- Install: `composer install`.
- Full gate (what CI runs): `composer check` = `composer validate --strict` +
  `composer normalize --dry-run` + `composer audit --abandoned=fail` + `format:check` +
  `analyse` + `rector:check` + `test` + `infection`.
- Individually: `composer format:check`, `composer analyse`, `composer rector:check`,
  `composer test`, `composer infection` (needs pcov or xdebug; see `README.md` for a local
  pcov build with Homebrew PHP).
- One test: `./vendor/bin/phpunit --filter ContractIdIsoTest` (or a method name).
- Apply formatting and headers: `composer format`; apply Rector: `composer rector`. Run
  `composer format` again after `composer rector`, the two tools converge in one round.
- After editing `composer.json`: `composer normalize`.

## Code Style

- PHPStan level 10 + strict rules is a hard gate on `src/` and `tests/`: full native types, no
  `mixed` leaks, `preg_match(...) === 1` rather than truthiness, no ignore entries.
- Rector dry run is a hard gate: if Rector wants to change a file, apply it (or skip the rule in
  `rector.php` with a comment explaining why, as done for the two PHPUnit rules).
- Mirror the Scala shape: strict factories throw `InvalidArgumentException`, forgiving parsers
  return `?T`, `readonly` value objects, canonical `__toString()`; uppercase once at the factory.
- Keep the check-digit code free of static caches (Infection attributes lazily initialised code
  to the first test only); every mutant on it must be killed by the fixtures in
  `tests/fixtures/`.
- Tests use `self::assert*()`; compare value objects with `assertEquals` (structural equality),
  scalars with `assertSame`; assert exception messages, not only exception classes.

## Release

- PHP is released by the common `Release` workflow (`.github/workflows/release.yml`, dispatched
  with the version): `Preflight PHP` runs `composer check`, `Release PHP` pushes the
  `git subtree split` of `php/` to the mirror repository as `vX.Y.Z`. Packagist follows the
  mirror. Setup and secrets: `CONTRIBUTING.md`.
