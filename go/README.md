# mobilityid-go

This document outlines the design and tooling choices for the `mobilityid-go` module,
which is a Go port of the `mobilityid` Scala library.

## Goals

- Provide an idiomatic Go API while documenting all intentional differences.
- Maintain quality through `gofmt`, `go vet`, and `golangci-lint`.

## Module Structure

The module is located under `go/mobilityid` and provides the core domain model,
validation, and parsing helpers for mobility identifiers.

Module path: `dev.juherr.mobilityid`

## API Design Decisions

- **Idiomatic Go:** The Go API is designed to be idiomatic, not a direct 1:1 Scala mirror.
- **Parsing Functions:** Functions like `NewCountryCode`, `NewProviderID`, etc.,
  return `(*T, error)` for expected invalid input.
- **Strict Factories:** `New...` functions act as strict factories, returning errors
  on invalid input rather than panicking, ensuring robust error handling.
- **Immutability:** Domain types (e.g., `CountryCode`, `ContractID`) are
  implemented as immutable structs with unexported fields, promoting data integrity.
- **Canonical Rendering:** The `String()` method is implemented for types
  (satisfying `fmt.Stringer` interface) to provide their canonical string representation.
- **Component Accessors:** For composite IDs like `ContractID`, accessor methods
  (`CountryCode()`, `ProviderID()`, `InstanceValue()`) are provided to retrieve individual components.
- **Scala parity first:** The Go port aims to support at least all Scala `core` capabilities.
  Go-specific extensions are allowed, but never as a replacement for Scala-compatible behavior.

## Tooling

- **Go Toolchain:** Go 1.22+ is used for building and testing.
- **Tests:** The standard Go testing package is used for unit and integration tests.
  Table-driven tests are employed for comprehensive case coverage.
- **Formatting:** `gofmt` is enforced to maintain consistent code style.
- **Static Analysis:**
    - `go vet` is integrated into the build process.
    - `golangci-lint` provides comprehensive linting, configured via `.golangci.yml`.

## Current Status (as of 2026-02-16)

The following core identifiers and models have been ported:

- `CountryCode`
- `PhoneCountryCode`
- `ProviderID`
- `OperatorIDISO`
- `OperatorIDDIN`
- `PartyID`
- `ContractIDStandard`
- `ContractID` (with parsing, rendering, and ISO/EMI3/DIN conversions)
- `EvseID`
- `EvseIDISO`
- `EvseIDDIN`

Parser helper APIs (`ParseCountryCode`, etc.) are available, wrapping the `New...` constructors.

EVSE IDs are available both from full strings and from discrete fields via:

- `NewEvseIDFromParts` / `ParseEvseIDFromParts`
- `NewEvseIDISOFromParts` / `ParseEvseIDISOFromParts`
- `NewEvseIDDINFromParts` / `ParseEvseIDDINFromParts`

Implemented behavior includes:

- ISO 7064 Mod 37,2 and DIN 7064 Mod XY check-digit calculation and verification.
- Canonical formatting and normalization for Contract IDs and EVSE IDs.
- Party ID extraction from `ContractID` and `EvseIDISO`.
- Table-driven tests for identifiers, contracts, check digits, EVSE parsing, and parser helpers.

## Further Work

- Expand test parity against full Scala specifications.
- Refine parsing and error messaging for edge cases.
- Decide on publication flow for `mobilityid` artifacts.
