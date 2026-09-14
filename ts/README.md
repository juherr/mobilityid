# @juherr/mobilityid

TypeScript port of the Mobility ID domain library, inspired by the original Scala library
(`scala/` in this repository, created by The New Motion): same domain model, same identifiers,
same test fixtures, written for TypeScript.

## Goals

- Keep behavior aligned with the Scala implementation during migration.
- Provide an idiomatic TypeScript API with strict/tolerant parsing.
- Enforce quality gates from day one (lint, format, typecheck, tests, license headers).

## Tooling

- Node.js: 24 LTS primary target (CI also checks 22 and 25); `engines.node >= 22`
- Package manager: Bun, via `vp install`
- Unified toolchain: Vite+ (`vp`): `vp check` runs the formatter, Oxlint (type-aware) and the
  type checker; `vp test` is Vitest; `vp pack` is tsdown
- TypeScript is an explicit devDependency (6.x, the range Vite+ supports): Vite+ does not ship
  `tsc`, and `scripts/verify-package.sh` compiles a consumer project with it
- Property-based tests: fast-check (`test/properties.test.ts`)
- Coverage: `@vitest/coverage-v8`, thresholds in `vite.config.ts` (statements, functions and lines
  90 %, branches 85 %), enforced by `vp test --coverage` (`bun run test`, `bun run check`, CI);
  a filtered run (`vp test ContractId`) skips them on purpose
- Package shape: `publint --strict` and `@arethetypeswrong/cli` (`bun run check:shape`)
- License headers: Oxlint JS plugin (`@tony.ganchev/eslint-plugin-header`)

### TypeScript configuration

`tsconfig.json` is strict and emit-safe on purpose:

- `verbatimModuleSyntax`: type-only imports must be written `import type`; nothing is elided
  silently.
- `isolatedDeclarations` (with `declaration`): every exported symbol carries an explicit type or
  return type, so `.d.ts` files are produced per file without the type checker (`vp pack` emits
  them through oxc in a few milliseconds instead of running `tsc`).
- `erasableSyntaxOnly`: no `enum`, `namespace` or constructor parameter properties; the sources
  stay runnable by Node's type stripping. `ContractIdStandards` is an `as const` object for that
  reason.
- `target` and `lib` are both `ES2023`, which Node 22 (the minimum in `engines`) supports in
  full; `vp pack` additionally targets `node22.0.0` from `engines`.

## API design

- Tolerant parsing methods return `T | null`.
- Strict factories/parsers throw `TypeError` when inputs are invalid.
- `tryParse` returns a `ParseResult<T>`, a frozen discriminated union carrying either the value or
  the message the strict parser would throw, so callers get the reason without exceptions:

  ```ts
  import { ContractId, ContractIdStandards, MobilityIdParsers } from "@juherr/mobilityid";

  const result = ContractId.tryParse(ContractIdStandards.ISO, "NL-TNM-000122045-X");
  if (result.ok) {
    result.value.toCompactString();
  } else {
    result.error; // "Given check digit 'X' is not equal to computed 'U'"
  }
  MobilityIdParsers.tryParseEvseId("NL*TNM*840*6487").ok; // false, with both ISO and DIN reasons
  ```

  `parse` is derived from `tryParse`, so the three entry points share one validation path.
- Domain objects are immutable (`readonly` and frozen instances).
- Canonical rendering is preserved (`toString()`, compact rendering helpers).

## Commands

Run from `ts/`:

```bash
vp install
vp check                 # format + lint + types
vp test                  # or: vp test --coverage (thresholds), vp test ContractId (one suite)
vp pack
bun run lint             # headers included
bun run lint:fix
bun run check:shape      # publint --strict + attw
bun run check            # everything above, what CI runs
```

`vite.config.ts` is the single source of truth for TypeScript checks, test execution, formatting, coverage, and package build settings.

## Current scope

- Foundational identifiers: `CountryCode`, `PhoneCountryCode`, `ProviderId`, `OperatorIdIso`, `OperatorIdDin`, `PartyId`
- Check digits: ISO and DIN
- Contract model: `ContractId`, `ContractIdStandard`, format conversions
- EVSE model: `EvseId`, `EvseIdIso`, `EvseIdDin`
- Parser helper API: `MobilityIdParsers`

## License header policy

All TypeScript source and test files carry the Apache-2.0 header from `license-header.txt`
(copyright Julien Herr and contributors). It is enforced by Oxlint via the Vite+ `lint` config
using `@tony.ganchev/eslint-plugin-header`; `scripts/tests/header-policy.test.sh`
(`bun run check:headers`, also run in CI) proves the gate rejects a missing or an extra copyright
line. Use:

- `bun run lint` to validate
- `bun run lint:fix` to auto-fix

The package ships `LICENSE` (the full Apache 2.0 text) and `NOTICE` (the copyright line); the
release package check (`../scripts/verify-npm-package.sh`) fails when either is missing or when
`LICENSE` is only the short header instead of the full license.

## Package shape

- ESM only, `exports` only: the legacy `main`/`types` fields are not set because every supported
  runtime (`engines.node >= 22`, bundlers, TypeScript `NodeNext`/`bundler`) resolves through
  `exports`. `attw` runs with the `esm-only` profile, which ignores the expected CommonJS and
  `node10` failures; anything else fails `bun run check`.
- `sideEffects: false`, so bundlers can tree-shake unused identifiers.
- `publint --strict` and `attw` run on the source tree in `bun run check` and on the packed
  tarball in `scripts/verify-package.sh`.

### JSR

Evaluated and not adopted for now. The sources already satisfy JSR's "no slow types" rule
(`isolatedDeclarations` enforces the same explicit-type discipline), so a `jsr.json` plus
`npx jsr publish` (OIDC from GitHub Actions) would be a small addition. It is left out because
it doubles the release surface (a second registry to bootstrap, monitor and document in the
`Release` workflow) with no known consumer asking for it; JSR also publishes the TypeScript
sources, whose relative imports use `.js` extensions that Deno does not remap to `.ts`. Revisit
when a Deno consumer shows up.

## Publishing to npm

The package is `@juherr/mobilityid` (public, scoped). `package.json` keeps `0.0.0-development`;
the release version is stamped at release time. Only `dist/`, `README.md`, `LICENSE`, `NOTICE`
and `package.json` ship (`files` field). npm generates the provenance attestation itself because the
`Release` workflow publishes through Trusted Publishing (OIDC); no `publishConfig.provenance` flag
or token is needed.

### Validate the package locally

```bash
vp install
scripts/verify-package.sh            # or: scripts/verify-package.sh 1.2.3
```

It runs `bun run check` (format, lint, types, tests with coverage, build, package shape),
`npm pack`, asserts the tarball content (`../scripts/verify-npm-package.sh`, allowlist:
`package.json`, `README.md`, `LICENSE`, `NOTICE`, `dist/**`; full Apache 2.0 text and copyright
notice asserted), runs the pinned `publint --strict` and `attw`
on the tarball, then installs it in a throw-away project and uses it from Node (ESM) and
TypeScript (declarations resolve through `exports`, tolerant parsers are `T | null`, `tryParse`
narrows on `ok`). `package.json` is restored afterwards. The same script runs in
`ci-ts.yml` (Node 24) and in the `Release` preflight.

### Normal releases: GitHub Actions + npm Trusted Publishing (OIDC)

Releases are cut by the dispatched `Release` workflow (`.github/workflows/release.yml`, see the
root `CONTRIBUTING.md`). The `release-ts` job publishes the tarball verified by the preflight with
`npm publish`, authenticated through GitHub OIDC (`permissions.id-token: write`, Node 24 with
`registry-url`, `package-manager-cache: false`, npm >= 11.5.1). npm generates the provenance
attestation itself. **No npm token is stored in GitHub Secrets, and none must be added**
(`NPM_TOKEN`, `NODE_AUTH_TOKEN`, granular tokens included).

### One-time bootstrap: the first publication

Trusted publishing is configured per package on npmjs.com, so the package must exist first.
The first version is published manually, with your npm account and 2FA:

1. Merge the packaging and workflow configuration, then on `main`:
   ```bash
   cd ts
   vp install
   scripts/verify-package.sh X.Y.Z            # builds build/npm-package/juherr-mobilityid-X.Y.Z.tgz
   npm login                                  # interactive, 2FA
   npm publish build/npm-package/juherr-mobilityid-X.Y.Z.tgz   # add --tag next for a prerelease
   npm logout
   ```
   `publishConfig` already sets `access: public` and the registry. Use the version you will
   also record in `CHANGELOG.md` / `.github/release-notes/X.Y.Z.md` so the first Java and
   TypeScript versions stay aligned.
2. On npmjs.com → package `@juherr/mobilityid` → **Settings** → **Trusted publishing** →
   **Add trusted publisher** → GitHub Actions:
   - Organization or user: `juherr`
   - Repository: `mobilityid`
   - Workflow filename: `release.yml` (filename only; it must not be renamed)
   - Environment name: `npm` (the `release-ts` job runs in this GitHub environment)
   - Allowed actions: configurations created after 2026-09-03 allow `npm stage publish` only by
     default; **enable direct `npm publish`** as well, since the workflow publishes directly.
3. Still in package settings → **Publishing access**: select _Require two-factor authentication
   and disallow tokens_, so only OIDC and interactive 2FA publishes are accepted.
4. Every later version goes through the `Release` workflow only.

`repository.url` in `package.json` must keep matching `https://github.com/juherr/mobilityid`:
npm checks it against the OIDC claims.

### Testing the pipeline without publishing

- `scripts/verify-package.sh` locally or in `ci-ts.yml` exercises everything up to, but not
  including, `npm publish`.
- The `Release` workflow only runs on manual dispatch from `main` with an explicit version, after
  `validate` and both preflights; a version that already exists on npm is skipped. A dry run of
  the publish step itself is `npm publish <tarball> --dry-run` from a local checkout.
