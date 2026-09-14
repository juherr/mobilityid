# AGENTS.md (ts workspace)

TypeScript port `@juherr/mobilityid`. Shared guidance (domain model, parity rule, license header
text, release tags) is in the root `AGENTS.md`; API design and scope are in `README.md`. Run every
command below from `ts/`.

## Layout

- `src/` -> one file per identifier (`contract-id.ts`, `evse-id.ts`, ...), check digits
  (`check-digit-iso.ts`, `check-digit-din.ts`), `parsers.ts` (`MobilityIdParsers`), `index.ts`
  is the public barrel: export new public symbols there.
- `test/` -> Vitest parity suites; `properties.test.ts` holds the fast-check properties (check
  digits, render/parse round trips, `parse`/`parseStrict`/`tryParse` agreement): extend it when
  adding a parser.
- `vite.config.ts` -> Vite+ config for check/test/pack, coverage thresholds; `license-header.txt`
  -> header template used by the Oxlint header plugin.

## Toolchain

- Bun (package manager) + Vite+ (`vp`), Node 24 (`mise.toml`). Not pnpm, not bare Vitest.
- TypeScript 6 is an explicit devDependency (Vite+ does not ship `tsc`); `tsconfig.json` sets
  `verbatimModuleSyntax`, `isolatedDeclarations` and `erasableSyntaxOnly` (`README.md`
  "TypeScript configuration"): exported functions and constants need explicit types, type-only
  imports use `import type`, no `enum`/`namespace`/parameter properties.
- Oxlint with `@tony.ganchev/eslint-plugin-header` enforces the Apache header on `src/**` and `test/**`.
- Header: `license-header.txt`, Julien Herr only (root `AGENTS.md` "Provenance"); `bun run check:headers` proves the gate.
- CI (`.github/workflows/ci-ts.yml`): `vp check`, `vp test --coverage`, `vp pack`, `bun run lint`,
  `bun run check:headers`, `bun run check:shape`, then `scripts/verify-package.sh` on Node 24.
- Published to npm by the dispatched `Release` workflow through npm Trusted Publishing (OIDC, no token; `README.md` "Publishing to npm"); `package.json` version stays `0.0.0-development`. Only `dist/`, `README.md`, `LICENSE` (full Apache 2.0 text), `NOTICE` ship (`files`).

## Commands

- Install: `vp install`.
- Typecheck / test / build: `vp check` (format + lint + types), `vp test --coverage`, `vp pack`;
  `bun run check` runs them plus `bun run check:shape` (`publint --strict`, `attw`).
- One test pattern: `vp test ContractId` (no coverage thresholds on a filtered run).
- Package check (pack, content, publint, throw-away Node + TypeScript consumer): `scripts/verify-package.sh [version]`; runs in CI on Node 24.
- Lint + headers: `bun run lint`; fix: `bun run lint:fix`.
- Format: `bun run format`, verify: `bun run format:check`. Markdown is excluded from `vp fmt` (`vite.config.ts`): the macOS and Linux oxfmt binaries disagree on the final newline.

## Code Style

- Three parsing entry points per identifier, sharing one validation path: strict
  (`from`/`parseStrict`, throws `ValidationError`, a `TypeError` subclass from
  `parse-result.ts`; never throw a bare `TypeError` for invalid input), `tryParse`
  (`ParseResult<T>`, frozen `{ ok: true, value } | { ok: false, error }` with the strict message)
  and tolerant `parse` (`T | null`, derived from `tryParse`). Only `ValidationError` is captured;
  anything else propagates. Mirror all three in `MobilityIdParsers`.
- Domain objects are immutable (`readonly`, frozen); canonical `toString()` plus compact
  rendering helpers.
- ESM only with `.js` extensions in relative imports (`./parsers.js`).
