# Mobility ID PHP Library

This library provides a PHP port of the Scala Mobility ID domain model, offering an idiomatic PHP API for handling various mobility identifiers like Country Codes, Provider IDs, Contract IDs, and EVSE IDs, along with their associated validation and check-digit algorithms.

## Goals

- Build a PHP 8.3+ port in a separate `php/` workspace.
- Keep Scala (`sbt`) code as the behavior reference during migration.
- Provide an idiomatic PHP API while documenting all intentional differences.
- Enforce quality from day one: `pint` (Laravel), `PHPStan`.

## Chosen Architecture

- New Composer project under `php/` for easy later extraction.
- Module: `mobility-id`: domain model + validation + check-digit algorithms + PHP-friendly parsing helpers.
- Namespace: `Juherr\MobilityId`.
- Structure: PSR-4 autoloading (`src/` for source files, `tests/` for test files).

## Tooling Decisions

- **Build/Dependencies**: Composer
- **PHP**: 8.3+
- **Tests**: PHPUnit
- **Formatting**: `friendsofphp/php-cs-fixer` (PSR-12)
- **Static analysis**: `phpstan/phpstan` (level 10) with strict rules
- **CI matrix**: PHP 8.3, 8.4, 8.5

## API Design Decisions

- PHP API is idiomatic, not a 1:1 Scala mirror.
- Parsing functions return nullable types (`?T`) for expected invalid input (e.g., `EvseId::opt`).
- Strict factories (`of(...)`) throw `InvalidArgumentException`.
- Domain types are immutable (`readonly` classes/properties). _(Note: `readonly` keyword on classes had to be temporarily omitted due to a bug in PHP 8.5.3 affecting static properties, pending PHP update)_
- Canonical rendering preserved (`__toString()` method).

## Core Components Implemented

### Foundational Identifiers
-   `CountryCode`: ISO 3166-1 alpha-2 country codes (e.g., "NL", "DE").
-   `PhoneCountryCode`: Phone country codes (e.g., "+31", "+49").
-   `ProviderId`: Three-letter identifier for providers (e.g., "TNM", "ABC").
-   `OperatorIdIso`: ISO-style operator ID (three-letter alphanumeric).
-   `OperatorIdDin`: DIN-style operator ID (three to six digit numeric).
-   `PartyId`: Combination of CountryCode and Provider/Operator ID (e.g., "NL-TNM").

### Check-Digit Algorithms
-   `CheckDigitIso`: ISO 15118-1 compliant check digit calculation.
-   `CheckDigitDin`: DIN SPEC 91286 compliant check digit calculation.

### Contract Model
-   `AbstractContractId`: Base class for contract IDs.
-   `ContractIdStandard\Iso`, `ContractIdStandard\Emi3`, `ContractIdStandard\Din`: Marker interfaces for different contract ID standards.
-   `ContractIdIso`: Concrete implementation for ISO 15118-1 contract IDs.
-   `ContractIdEmi3`: Concrete implementation for EMI3 contract IDs.
-   `ContractIdDin`: Concrete implementation for DIN SPEC 91286 contract IDs.
-   `ContractIdParser`: Utility class for parsing and validating contract ID strings according to different standards.

### EVSE Model
-   `AbstractEvseId`: Base class for EVSE IDs.
-   `EvseIdStandard\Iso`, `EvseIdStandard\Din`: Marker interfaces for different EVSE ID standards.
-   `EvseIdIso`: Concrete implementation for ISO EVSE IDs.
-   `EvseIdDin`: Concrete implementation for DIN EVSE IDs.
-   `EvseIdParser`: Utility class for parsing and validating EVSE ID strings according to different standards.
-   `EvseId`: Main factory class for creating EVSE IDs (can parse both ISO and DIN formats).

## How to Install

Navigate to the `php/` directory and run Composer install:

```bash
composer install
```

## How to Run Tests

From the `php/` directory:

```bash
composer test
```

## How to Run Static Analysis

From the `php/` directory:

```bash
composer analyse
```

## How to Run Formatter

From the `php/` directory:

```bash
composer format
```

This command runs `php-cs-fixer` to automatically fix coding style issues according to the configured PSR-12 standard.

## How to Run Format Check (CI mode)

From the `php/` directory:

```bash
composer format:check
```

This command runs `php-cs-fixer` in dry-run mode and fails when formatting changes are needed.
