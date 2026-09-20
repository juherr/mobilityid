# AGENTS.md (ts workspace)

TypeScript port `@juherr/mobilityid`. Shared guidance (domain model, parity rule, license header
text, release tags) is in the root `AGENTS.md`; API design and scope are in `README.md`. Run every
command below from `ts/`.

## Layout

- `src/` -> one file per identifier (`contract-id.ts`, `evse-id.ts`, ...), `string-id.ts` (`StringId`,
  `StringIdSpec`, `defineStringId`), check digits (`check-digit-iso.ts`, `check-digit-din.ts`),
  `parsers.ts` (`MobilityIdParsers`), `index.ts` is the public barrel: export new public symbols
  there.
- `test/` -> Vitest parity suites; `properties.test.ts` holds the fast-check properties (check
  digits, render/parse round trips, `parse`/`parseStrict`/`tryParse` agreement): extend it when
  adding a parser.
- `vite.config.ts` -> Vite+ config for check/test/pack, coverage thresholds; `license-header.txt`
  -> header template used by the Oxlint header plugin.

## Toolchain

- Bun (package manager, the version in `packageManager` is what `vp`/`setup-vp` run; `engines.bun`
  is the consumer floor) + Vite+ (`vp`), Node 24 (`mise.toml`). Not pnpm, not bare Vitest.
- `@vitest/coverage-v8` is pinned to the exact Vitest version Vite+ bundles (`vp --version`):
  Vite+ refuses any other one at startup. Bump it by hand with the `vite-plus` update that moves
  the bundled Vitest; Renovate is told not to touch it (`.github/renovate.json`).
- TypeScript 6 is the supported baseline and an explicit devDependency (Vite+ does not ship
  `tsc`); Renovate keeps it `<7`. TypeScript 7 is a compatibility canary only (`ts-next` job in
  `ci-ts.yml`: TS 7 `tsc` on sources/tests/config, then `scripts/verify-package.sh` with it).
  `tsconfig.json` sets
  `verbatimModuleSyntax`, `isolatedDeclarations` and `erasableSyntaxOnly` (`README.md`
  "TypeScript configuration"): exported functions and constants need explicit types, type-only
  imports use `import type`, no `enum`/`namespace`/parameter properties.
- Oxlint with `@tony.ganchev/eslint-plugin-header` enforces the Apache header on `src/**` and `test/**`.
- Header: `license-header.txt`, Julien Herr only (root `AGENTS.md` "Provenance"); `bun run check:headers` proves the gate.
- CI (`.github/workflows/ci-ts.yml`): `vp check`, `vp test --coverage`, `vp pack`, `bun run lint`,
  `bun run check:headers`, `bun run check:shape`, then `scripts/verify-package.sh` on Node 24
  (TypeScript 6, the baseline); a separate `ts-next` job repeats the type-check and the package
  verification with the latest TypeScript 7.
- Published to npm by the dispatched `Release` workflow through npm Trusted Publishing (OIDC, no token; `README.md` "Publishing to npm"); `package.json` version stays `0.0.0-development`. Only `dist/`, `README.md`, `LICENSE` (full Apache 2.0 text), `NOTICE` ship (`files`).

## Commands

- Install: `vp install`.
- Typecheck / test / build: `vp check` (format + lint + types), `vp test --coverage`, `vp pack`;
  `bun run check` runs them plus `bun run check:shape` (`publint --strict`, `attw`).
- One test pattern: `vp test ContractId` (no coverage thresholds on a filtered run).
- Package check (pack, content, publint, throw-away Node + TypeScript consumer): `scripts/verify-package.sh [version]`; runs in CI on Node 24.
- Lint + headers: `bun run lint`; fix: `bun run lint:fix`.
- ISO 3166-1 table: `bun run generate:iso3166` rewrites `src/iso3166-alpha2.ts` from the JDK's
  `Locale.getISOCountries()` through `../go/scripts/Iso3166Codes.java` (needs the Java version
  pinned in `../mise.toml`). `scripts/generate-iso3166.sh --check` regenerates to a temporary
  file and fails on any difference; CI runs it on the Node 24 job with Temurin 21 (keep that
  version in step with `mise.toml`). `test/country-code.test.ts` asserts the count (249, bump it
  with the JDK) and equality with the Go table, so regenerate both ports together.
- Format: `bun run format`, verify: `bun run format:check`. Markdown is excluded from `vp fmt` (`vite.config.ts`): the macOS and Linux oxfmt binaries disagree on the final newline.

## Code Style

- Three parsing entry points per identifier, sharing one validation path: strict
  (`from`/`parseStrict`, throws `ValidationError`, a `TypeError` subclass from
  `parse-result.ts`; never throw a bare `TypeError` for invalid input), `tryParse`
  (`ParseResult<T>`, frozen `{ ok: true, value } | { ok: false, error }` with the strict message)
  and tolerant `parse` (`T | null`, derived from `tryParse`). Only `ValidationError` is captured;
  anything else propagates. Mirror all three in `MobilityIdParsers`.
- Single-valued identifiers are plain strings with a companion: `export type X = StringId<"X">`
  (a brand, for an open set of values) plus
  `export const X: StringIdCompanion<X> = defineStringId({ isValid, message })` in the same file
  (type and value share the name). No wrapper class, no `.value`; add a new one the same way.
  `CountryCode` is the exception: a closed set, so its type is the literal union of the generated
  `ISO_3166_ALPHA2` table and only its companion goes through `defineStringId`.
  Composite identifiers (`PartyId`, `ContractId`, `EvseId*`) are immutable classes (`readonly`,
  frozen) holding those strings; canonical `toString()` plus compact rendering helpers.
- ESM only with `.js` extensions in relative imports (`./parsers.js`).
