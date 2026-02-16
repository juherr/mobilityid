# @juherr/mobilityid

TypeScript port of the Mobility ID domain library.

## Goals

- Keep behavior aligned with the Scala implementation during migration.
- Provide an idiomatic TypeScript API with strict/tolerant parsing.
- Enforce quality gates from day one (lint, format, typecheck, tests, license headers).

## Tooling

- Node.js: 24 LTS primary target (CI also checks 22 and 25)
- Package manager: `pnpm`
- TypeScript: strict mode
- Tests: Vitest
- Lint: ESLint (`@typescript-eslint` strict)
- Formatting: Prettier

## API design

- Tolerant parsing methods return `T | null`.
- Strict factories/parsers throw `TypeError` when inputs are invalid.
- Domain objects are immutable (`readonly` and frozen instances).
- Canonical rendering is preserved (`toString()`, compact rendering helpers).

## Commands

Run from `ts/`:

```bash
pnpm install
pnpm lint
pnpm format:check
pnpm typecheck
pnpm test
pnpm build
pnpm license:check
pnpm license:apply
pnpm check
```

## Current scope

- Foundational identifiers: `CountryCode`, `PhoneCountryCode`, `ProviderId`, `OperatorIdIso`, `OperatorIdDin`, `PartyId`
- Check digits: ISO and DIN
- Contract model: `ContractId`, `ContractIdStandard`, format conversions
- EVSE model: `EvseId`, `EvseIdIso`, `EvseIdDin`
- Parser helper API: `MobilityIdParsers`

## License header policy

All TypeScript source and test files must include the repository Apache-2.0 header.
Use:

- `pnpm license:check` to validate
- `pnpm license:apply` to fix
