# AGENTS.md

Guidance for coding agents working in this repository. `CLAUDE.md` imports this file and adds Claude Code specifics.

## Readme map

- Monorepo overview and quick commands: `README.md`.
- Scala/original library documentation and usage examples: `scala/README.md`.
- Java (`mobilityid4j`) documentation and API/tooling choices: `java/README.md`.
- Go, PHP and TypeScript design choices: `go/README.md`, `php/README.md`, `ts/README.md`.

## Project Snapshot

- Multi-workspace repository:
  - `scala/` -> legacy/primary Scala implementation (sbt, specs2).
  - `java/` -> Java 21 port (`mobilityid4j`) using Gradle.
  - `go/` -> Go port (`mobilityid.juherr.dev/go`) using Go toolchain.
  - `php/` -> PHP port (Composer package `juherr/mobility-id`, PHP 8.4+).
  - `ts/` -> TypeScript port (`@juherr/mobilityid`) using Bun + Vite+ instead of pnpm + Vitest.
- Tool versions are pinned in `mise.toml` (Java, Node, sbt, Gradle, PHP, Go, golangci-lint).
- Each workspace has its own `AGENTS.md` (commands, toolchain, style) and `README.md` (API design choices). Read the workspace `AGENTS.md` before working in it; this file only holds what is shared.

## Repository Layout

- `scala/core` (domain reference) and `scala/interpolators` (compile-time interpolators, `scala-2/` and `scala-3/` sources) -> `scala/AGENTS.md`.
- `java/src/main/java/dev/juherr/mobilityid4j` (+ `interpolators/MobilityIdParsers`) -> `java/AGENTS.md`.
- `go/mobilityid` -> `go/AGENTS.md`.
- `php/src`, `php/tests` (namespace `Juherr\MobilityId`) -> `php/AGENTS.md`.
- `ts/src` (`index.ts` barrel, `parsers.ts`), `ts/test` -> `ts/AGENTS.md`.
- `docs/` -> GitHub Pages site for the Go vanity import path (`mobilityid.juherr.dev`, `go-import` meta tag). Do not add docs there.

## Domain Architecture

All workspaces implement the same model; Scala (`scala/core`) is the behavior reference and the ports
mirror its test fixtures. Read `ContractId.scala`, `EvseId.scala`, `basicIdentifiers.scala` and
`checkDigit.scala` to understand the whole model before touching any port.

- Basic identifiers: `CountryCode` (ISO alpha-2), `PhoneCountryCode` (`+49` style, used by DIN),
  `ProviderId`/`PartyCode` (3 alphanumeric chars), `PartyId` (country + party code), `OperatorIdIso`
  and `OperatorIdDin`. All are `sealed trait` + private case class, constructed via companion `apply`
  that normalizes to uppercase and throws `IllegalArgumentException` on invalid input.
- `ContractId[T <: ContractIdStandard]` has three standards: `ISO` (ISO 15118), `EMI3` and `DIN`
  (DIN SPEC 91286). Each standard has a `ContractIdParser` (regex + check-digit validation) and
  conversions are expressed as `ContractIdConverter[From, To]` type-class instances
  (`DIN<->EMI3`, `EMI3->ISO`; `ISO<->DIN` are deprecated). Ports model this as one class per
  standard (`ContractIdIso`, `ContractIdEmi3`, `ContractIdDin`) plus explicit conversion methods.
- `EvseId` has two formats, `EvseIdIso` (country code + operator + power outlet, optional `*`
  separators) and `EvseIdDin` (phone country code + DIN operator). `EvseId(string)` tries ISO then DIN.
- Check digits: `CheckDigitIso` is the ISO 15118 / EMI3 matrix-based algorithm (over a 14-char
  payload), `CheckDigitDin` is the DIN algorithm. They are pure functions shared by contract
  parsers; never change them without cross-language parity tests.
- Parsing entry points are the same shape in every language: strict `apply`/constructor that throws,
  a forgiving `opt`-style helper returning `Option`/`null`/`undefined`, and "interpolator"/parser
  helpers (Scala compile-time interpolators, `MobilityIdParsers` in Java/TS, `*Parser` classes in
  PHP, `parser.go` in Go).
- When adding behavior, add it to Scala first (or confirm it exists there), then port it with the
  same test cases to every workspace so parity stays verifiable.

## Build, Lint, and Test Commands

Every workspace is self-contained; run its commands from its own directory and see its `AGENTS.md`
for single-suite and lint invocations. Full gates per workspace:

| Workspace | Full gate (what CI runs) | Single suite |
|---|---|---|
| `scala/` | `sbt headerCheck test` (cross: `sbt +test`) | `sbt "core/testOnly *ContractIdSpec"` |
| `java/` | `./gradlew check` | `./gradlew test --tests "*ContractIdTest"` |
| `go/` | `golangci-lint run && go vet ./... && go test ./...` | `go test ./... -run TestContractID` |
| `php/` | `composer check` (needs pcov or xdebug for Infection) | `./vendor/bin/phpunit --filter ContractIdIsoTest` |
| `ts/` | `bun run lint && bun run check` | `vp test ContractId` |

### CI and release

- One CI workflow per workspace (`.github/workflows/ci-{scala,java,go,php,ts}.yml`); only touch the workflow of the workspace you changed.
- `Release` (`release.yml`) is dispatched manually from `main` with the version as input: it checks `CHANGELOG.md` and `.github/release-notes/X.Y.Z.md`, runs the Java, TypeScript and PHP preflights, and only when all pass publishes Java (Maven Central Portal via nmcp), TypeScript (npm via Trusted Publishing/OIDC, no token) and PHP (a `git subtree split` of `php/` pushed to the `juherr/mobility-id-php` mirror that Packagist follows, via a deploy key) idempotently, then creates the signed `vX.Y.Z` tag and the GitHub Release. Go uses `go/vX.Y.Z` tags (`release-go.yml`). Full procedure in `CONTRIBUTING.md`.
- `CI Workflows` (`ci-workflows.yml`) lints every workflow with actionlint and zizmor; every checkout uses `persist-credentials: false` and publishing workflows disable caches.
- Security gates: `dependency-submission.yml` submits the resolved Gradle graph (GitHub cannot parse Gradle) on `main` and same-repository PRs; dependency review on pull requests (same workflow, job after the submission, fails on high+ in any scope); fork and Dependabot PRs get their Java graph submitted and reviewed by `fork-dependency-graph.yml` after provenance validation (see `CONTRIBUTING.md`); Dependabot alerts on `main`; weekly/on-demand OWASP Dependency-Check full scan (`security.yml`, not a PR gate).

### License headers

All source files carry the same Apache 2.0 header, enforced per workspace (sbt-header, Spotless,
golangci-lint `goheader`, php-cs-fixer, Oxlint header plugin). Each workspace `AGENTS.md` gives the
validate/apply commands; CI fails on a missing header.

**Provenance.** `scala/core` is the modified New Motion code and keeps the 2014 notice (first
header line, `scala/build.sbt`). `ts/` is a port inspired by the Scala library and credits Julien
Herr only, by the maintainer's decision: its header template is `ts/license-header.txt`, its
`README.md` states the inspiration, and `ts/scripts/tests/header-policy.test.sh` proves the gate
rejects any other copyright line. `php/` follows the same policy (header template in
`php/.php-cs-fixer.dist.php`, Scala library credited in `php/README.md`). `java/` and `go/`
currently keep both lines. Root
`LICENSE` is the full Apache 2.0 text; root `NOTICE` lists the workspaces and which notice
applies to each. Packaged artifacts ship the full license text (`ts/LICENSE`) plus a `NOTICE`
with the copyright line, asserted by `scripts/verify-npm-package.sh`.

**License header format** (`scala/`, `java/`, `go/`; `php/` and `ts/` omit the 2014 line):
```
Copyright (c) 2014 The New Motion team, and respective contributors
Copyright (c) 2026 Julien Herr, and respective contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Agent Workflow Expectations

- Make the smallest safe change that solves the request.
- Keep changes workspace-scoped when possible; a domain change is the exception and must land in every workspace (see Domain Architecture).
- Run targeted tests first, then broaden to module/all tests as needed.
- Do not refactor broadly unless requested.
- Preserve public API compatibility unless the task explicitly allows breaking changes.
- Keep documentation current while implementing changes:
  - Update the workspace `AGENTS.md` whenever its commands, toolchain, or quality gates evolve; update this file only for cross-workspace changes (layout, domain, release).
  - Update the root `README.md` and the workspace `README.md` incrementally as features, API choices, or tooling decisions change.
  - Record practical lessons learned (pitfalls, conventions, migration notes) in the relevant README/guide instead of leaving them only in PR/chat context.
  - Add a line to `CHANGELOG.md` under `Unreleased` for any user-visible change (Common Changelog, grouped by impact, breaking changes marked).

## Code Style (shared)

Language-specific rules live in each workspace `AGENTS.md`. Across all ports:

- Model identifiers as immutable value types with validation centralized in one factory/constructor.
- Two entry points per identifier: a strict one that throws on invalid input and a forgiving one that returns the language's "absent" value.
- Normalize to uppercase where the domain requires it; validate by regex + semantic checks (country code, lengths, check digits); error messages name the failed field.
- Keep deterministic precedence when several parsers are tried (ISO before DIN for EVSE IDs).
- Preserve check-digit behavior exactly; every port ships the same positive and negative fixtures.
- Comments only for non-obvious intent (algorithm details, standards mapping).

## Change Safety Checklist

Before finalizing a change, an agent should:

1. Run targeted tests for touched suites.
2. Run the workspace full gate (table above) before finishing.
3. For domain changes, run the full gate of every workspace you touched and check the ports stay in parity.
4. Confirm no accidental API/signature changes.
5. Keep thrown error behavior backward compatible unless requested.

## Cursor and Copilot Rules

- Checked for Cursor rules in `.cursor/rules/` and `.cursorrules`: none found.
- Checked for Copilot instructions in `.github/copilot-instructions.md`: none found.
- If any of these files are added later, treat them as higher-priority local agent instructions and merge them into this guide.

## Renovate Configuration Notes

### mise.toml Version Format

The project uses simplified version formats in `mise.toml`:
- **Java/Node**: major only (`21`, `24`)
- **sbt/gradle/php/go**: major.minor (`1.12`, `9.7`, `8.4`, `1.26`)

This is enforced via `extractVersionTemplate` and `autoReplaceStringTemplate` in the customManager configuration in `.github/renovate.json`:

**Custom Manager (for mise.toml):**
```json
{
  "customType": "regex",
  "managerFilePatterns": ["/(^|/)mise\\.toml$/"],
  "matchStrings": ["(?<depName>[A-Za-z0-9_.-]+)\\s*=\\s*\"(?<currentValue>[^\"]+)\""],
  "datasourceTemplate": "asdf",
  "versioningTemplate": "loose",
  "extractVersionTemplate": "^(?:openjdk-)?(?<version>\\d+(?:\\.\\d+)?)",
  "autoReplaceStringTemplate": "{{depName}} = \"{{#if (equals depName 'java')}}{{newMajor}}{{else if (equals depName 'node')}}{{newMajor}}{{else}}{{newMajor}}.{{newMinor}}{{/if}}\""
}
```

**Consequences:**
- Patch updates (e.g., 1.12.3 → 1.12.4) will NOT trigger PRs for mise.toml
- Minor updates (e.g., 1.12 → 1.13) WILL trigger PRs and write X.Y format (not X.Y.Z)
- Major updates (e.g., 21 → 25) WILL trigger PRs and write X format for java/node, X.Y for others
- Major-only tools (Java/Node) will track major updates in `mise.toml`
- Build files keep full versions: `scala/project/build.properties` → `sbt.version=1.12.3`
- mise automatically uses the latest patch version available for the specified X.Y
- The `extractVersionTemplate` regex handles both regular versions and openjdk-prefixed versions
- The `autoReplaceStringTemplate` controls what format Renovate writes back to the file

Scala version policy (LTS + latest) is documented in `scala/AGENTS.md`.

## Notes for Future Agents

- Dependency updates are managed by Renovate (`.github/renovate.json`) for GitHub Actions, Gradle, sbt/Scala, and `mise.toml`.
- For feature parity work, mirror existing behavior from `scala/core` into each port incrementally, test cases first.
