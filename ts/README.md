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
- License headers: Oxlint JS plugin (`@tony.ganchev/eslint-plugin-header`)

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
bun run lint
bun run lint:fix
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
Headers are enforced by Oxlint via the Vite+ `lint` config using `@tony.ganchev/eslint-plugin-header`. Use:

- `bun run lint` to validate
- `bun run lint:fix` to auto-fix

## Publishing to npm

The package is `@juherr/mobilityid` (public, scoped). `package.json` keeps `0.0.0-development`;
the release version is stamped at release time. Only `dist/`, `README.md`, `LICENSE` and
`package.json` ship (`files` field).

### Validate the package locally

```bash
vp install
scripts/verify-package.sh            # or: scripts/verify-package.sh 1.2.3
```

It runs `bun run check` (typecheck, tests, build), `npm pack`, asserts the tarball content
(`../scripts/verify-npm-package.sh`, allowlist: `package.json`, `README.md`, `LICENSE`, `dist/**`), runs the pinned `publint`, then installs the tarball in a throw-away
project and uses it from Node (ESM) and TypeScript (declarations resolve through `exports`,
tolerant parsers are `T | null`). `package.json` is restored afterwards. The same script runs in
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
