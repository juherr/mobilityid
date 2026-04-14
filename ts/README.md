# @juherr/mobilityid

TypeScript port of the Mobility ID domain library.

## Goals

- Keep behavior aligned with the Scala implementation during migration.
- Provide an idiomatic TypeScript API with strict/tolerant parsing.
- Enforce quality gates from day one (lint, format, typecheck, tests, license headers).

## Tooling

- Node.js: 24 LTS primary target (CI also checks 22 and 25)
- Package manager: Bun, via `vp install`
- Unified toolchain: Vite+ (`vp`)
- TypeScript checks: `vp check`
- Tests: `vp test`
- Library packaging: `vp pack`
- License headers: ESLint (`eslint-plugin-header`)

## API design

- Tolerant parsing methods return `T | null`.
- Strict factories/parsers throw `TypeError` when inputs are invalid.
- Domain objects are immutable (`readonly` and frozen instances).
- Canonical rendering is preserved (`toString()`, compact rendering helpers).

## Commands

Run from `ts/`:

```bash
vp install
vp check
vp test
vp pack
bun run license:check
bun run license:apply
bun run check
```

`vite.config.ts` is the single source of truth for TypeScript checks, test execution, formatting, and package build settings.

## Current scope

- Foundational identifiers: `CountryCode`, `PhoneCountryCode`, `ProviderId`, `OperatorIdIso`, `OperatorIdDin`, `PartyId`
- Check digits: ISO and DIN
- Contract model: `ContractId`, `ContractIdStandard`, format conversions
- EVSE model: `EvseId`, `EvseIdIso`, `EvseIdDin`
- Parser helper API: `MobilityIdParsers`

## License header policy

All TypeScript source and test files must include the repository Apache-2.0 header.
Headers are enforced separately from Vite+ by ESLint (`eslint-plugin-header`). Use:

- `bun run license:check` to validate
- `bun run license:apply` to auto-fix
