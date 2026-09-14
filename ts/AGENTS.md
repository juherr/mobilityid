# AGENTS.md (ts workspace)

TypeScript port `@juherr/mobilityid`. Shared guidance (domain model, parity rule, license header
text, release tags) is in the root `AGENTS.md`; API design and scope are in `README.md`. Run every
command below from `ts/`.

## Layout

- `src/` -> one file per identifier (`contract-id.ts`, `evse-id.ts`, ...), check digits
  (`check-digit-iso.ts`, `check-digit-din.ts`), `parsers.ts` (`MobilityIdParsers`), `index.ts`
  is the public barrel: export new public symbols there.
- `test/` -> Vitest parity suites.
- `vite.config.ts` -> Vite+ config for check/test/pack; `license-header.txt` -> header template
  used by the Oxlint header plugin.

## Toolchain

- Bun (package manager) + Vite+ (`vp`), Node 24 (`mise.toml`). Not pnpm, not bare Vitest.
- Oxlint with `@tony.ganchev/eslint-plugin-header` enforces the Apache header on `src/**` and `test/**`.
- CI (`.github/workflows/ci-ts.yml`): `bun run lint`, `vp check`, `vp test`, `vp pack`.
- Published to npm by the dispatched `Release` workflow through npm Trusted Publishing (OIDC, no token; `README.md` "Publishing to npm"); `package.json` version stays `0.0.0-development`. Only `dist/`, `README.md`, `LICENSE` ship (`files`).

## Commands

- Install: `vp install`.
- Typecheck / test / build: `vp check`, `vp test`, `vp pack` (`bun run check` runs all three).
- One test pattern: `vp test ContractId`.
- Package check (pack, content, publint, throw-away Node + TypeScript consumer): `scripts/verify-package.sh [version]`; runs in CI on Node 24.
- Lint + headers: `bun run lint`; fix: `bun run lint:fix`.
- Format: `bun run format`, verify: `bun run format:check`.

## Code Style

- Tolerant parsers return `T | null`; strict factories throw `TypeError`; domain objects are
  immutable (`readonly`, frozen); canonical `toString()` plus compact rendering helpers.
- ESM only with `.js` extensions in relative imports (`./parsers.js`).