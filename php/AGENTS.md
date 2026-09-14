# AGENTS.md (php workspace)

PHP port, Composer package `juherr/mobility-id`, namespace `Juherr\MobilityId` (tests in
`Juherr\MobilityId\Tests`). Shared guidance (domain model, parity rule, license header text) is in
the root `AGENTS.md`; architecture and API decisions are in `README.md`. Run every command below
from `php/`.

## Layout

- `src/` -> one final class per identifier or standard (`ContractIdIso`, `ContractIdEmi3`,
  `ContractIdDin`, `EvseIdIso`, `EvseIdDin`, ...), shared bases (`AbstractContractId`,
  `AbstractEvseId`), `ContractIdParser` / `EvseIdParser` helpers, `CheckDigitIso` / `CheckDigitDin`.
- `tests/` -> PHPUnit suites, one `*Test.php` per class.

## Toolchain

- PHP 8.3+ locally (`mise.toml`); CI runs 8.3, 8.4 and 8.5 (`.github/workflows/ci-php.yml`).
- php-cs-fixer (formatting + Apache header via `HeaderCommentFixer`), PHPStan level 10, PHPUnit.

## Commands

- Install: `composer install`.
- Full gate (what CI runs): `composer check` = `format:check` + `analyse` + `test`.
- Individually: `composer format:check`, `composer analyse`, `composer test`.
- One test: `./vendor/bin/phpunit --filter ContractIdIsoTest` (or a method name).
- Apply formatting and headers: `composer format`.

## Code Style

- PHPStan level 10 is a hard gate: full native types, no `mixed` leaks, no suppressions without a
  comment explaining why.
- Mirror the Scala shape: strict constructors/factories throw `InvalidArgumentException`,
  forgiving parsers return `?T`, immutable readonly objects, canonical `__toString()`.
