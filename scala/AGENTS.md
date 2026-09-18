# AGENTS.md (scala workspace)

Scala reference implementation of the mobility ID domain. Shared, cross-workspace guidance
(domain model, parity rule, license header text, release tags) is in the root `AGENTS.md`.
Run every command below from `scala/`.

## Modules

- `core` -> domain types, parsers, check-digit algorithms (`src/main/scala/com/thenewmotion/mobilityid`).
  Published as `dev.juherr.mobilityid:mobilityid_{2.13,3}`.
- `interpolators` -> compile-time checked string interpolators; version-specific sources in
  `src/main/scala-2/` (`contextual-core` macros) and `src/main/scala-3/` (inline macros with
  `scala.quoted`), picked up by sbt automatically; shared specs in `src/test/scala/`.
  Published as `dev.juherr.mobilityid:mobilityid-interpolators_{2.13,3}`.
- `root` aggregates both (`publish / skip`).
- `consumer-smoke/` -> separate sbt build used by `scripts/verify-consumer.sh` only; it is not
  part of the aggregate and needs `SMOKE_REPOSITORY` + `MOBILITYID_VERSION` to load.

## Toolchain

- sbt 2.0.9 (`project/build.properties`, `mise.toml` `sbt = "2.0"`); the build definition is
  Scala 3 (bare settings apply to every project, no `ThisBuild`).
- `scalaVersion` 3.9.0, `crossScalaVersions` 2.13.18 / 3.9.0 (`build.sbt`). CI runs both
  (`.github/workflows/ci-scala.yml`). Bytecode target `-release 17`.
- Version: `RELEASE_VERSION` env (set by the `Release` workflow) or `-DreleaseVersion`,
  `0.1.0-SNAPSHOT` otherwise. There is no `version.sbt`.
- Test framework: specs2 4.23.0 (cross 2.13/3; specs2 5 is Scala 3 only).
- Plugins (`project/plugins.sbt`): sbt-header 5.11.0, sbt-scalafmt 2.6.2 (`.scalafmt.conf`,
  Scalafmt 3.11.5), sbt-scalafix 0.14.9 (`.scalafix.conf`), sbt-mima-plugin 1.2.1, sbt-pgp 2.3.2.

## Commands

Always pass sbt commands as one quoted, `;`-separated argument (sbt 2). Prefer
`sbt --server --batch "..."`: it runs in a fresh JVM, so environment variables are always the
current ones (the default thin client reuses a background server that keeps its initial
environment). Never rely on `sbt clean` to see compiler warnings again: sbt 2 replays cached
task results, run with a changed input instead.

- Compile / test everything: `sbt --server --batch "+compile"`, `sbt --server --batch "+test"`.
- One module: `sbt --server --batch "core/test"`, `sbt --server --batch "interpolators/test"`.
- One suite: `sbt --server --batch "core/testOnly com.thenewmotion.mobilityid.ContractIdSpec"`,
  pattern form `sbt --server --batch "core/testOnly *EvseIdSpec"`.
- One specs2 example: `sbt --server --batch "core/testOnly com.thenewmotion.mobilityid.ContractIdSpec -- -ex 'render a contract id in the normalized form with dashes and check digit'"`
  (`--` forwards args to specs2; `-ex <text>` includes, `-x <text>` excludes).
- One Scala version: `sbt --server --batch "++2.13.18; test"` (same for 3.9.0).
- Format and fix: `sbt --server --batch "scalafmtSbt; +scalafmtAll; +scalafixAll"`.
- Full gate (what CI runs per Scala version):
  `sbt --server --batch "+headerCheckAll; +scalafmtCheckAll; scalafmtSbtCheck; +scalafixAll --check; +test; +mimaReportBinaryIssues"`.
  CI adds `-Dmobilityid.mimaBaseline=X.Y.Z` from `scripts/mima-baseline.sh` (last Scala release on
  Maven Central; without it MiMa is skipped).
- Release preflight, as `release.yml` runs it: `scripts/verify.sh` = full gate +
  `scripts/verify-release-wiring.sh` (guard refuses SNAPSHOT / missing inputs / empty keyring,
  `publishSigned` stages nothing when the guard fails, MiMa analyzes a locally published
  baseline) + `scripts/verify-consumer.sh` (publishes to `target/smoke-repo` and runs
  `consumer-smoke/` on both Scala versions). Log in `target/verification/verify.log`.
- License headers: `sbt --server --batch "+headerCheckAll"` to validate,
  `sbt --server --batch "+headerCreateAll"` to apply (main and test sources).

## Code Style

Core code must compile on Scala 2.13 and 3.9; keep it cross-compatible (Scala 2 syntax,
`_` wildcards: Scalafix `targetDialect = Scala2`).

- Scalafmt owns layout (2 spaces, 120 columns, no vertical alignment); Scalafix owns imports
  (`OrganizeImports`: Scala/JDK, third-party, then `com.thenewmotion` project imports, merged
  selectors) and removes unused symbols. Run both before opening a PR.
- Warnings are errors on both versions; silence a warning only with a targeted `@nowarn(...)`
  and a comment (see `ContractIdConverter`: Scala 3 reports the deprecated converters on the
  enclosing object; `-Wconf:cat=unused-nowarn:s` keeps 2.13 from rejecting that annotation).
- Types/traits `PascalCase`, objects/vals/defs `camelCase`; `PascalCase` vals only where the file
  already does it (parser regex fields like `FullRegex`). Suites end with `Spec`. Established domain
  abbreviations (ISO, DIN, EMI3, EVSE) are fine, others are not.
- Model with `sealed trait` + private case class; centralize validation in companion `apply`;
  explicit return types on public methods; `Option` when absence is expected
  (`EvseId(string): Option[...]`), `IllegalArgumentException` in strict constructors,
  `Try(...).toOption` for `opt`-style helpers.
- Normalize to uppercase where the domain requires it; validate by regex + semantic checks;
  keep error messages specific to the failed field; keep deterministic precedence when combining
  candidate parsers; do not swallow exceptions except when intentionally converting to `Option`/`Either`.
- Comments only for non-obvious intent (algorithm details, standards mapping); concise
  domain-oriented ScalaDoc.
- Public API changes are checked by MiMa against the last release: a binary-incompatible change
  needs a `mimaBinaryIssueFilters` entry with a justification and a major/minor bump.

## Testing (specs2)

- Mutable style: `class XSpec extends Specification`, nested `"Subject" should { ... }` blocks.
- Cover positive and negative parsing cases, normalization and rendering (`toString`, compact
  forms), thrown exception types/messages, and round trips for format conversions.
- Use `beSome[T].which(...)` with an explicit type parameter for Scala 3 inference.

## Scala LTS Version Policy

`scalaVersion` follows the Scala 3 LTS line (3.9 since September 2026) and `crossScalaVersions`
carries exactly one Scala 3 version next to 2.13: the `mobilityid_3` artifact is built with the
LTS, and a second 3.x version would publish the same artifact twice. Enforced in
`.github/renovate.json`:

```json
{
  "matchManagers": ["sbt"],
  "matchPackageNames": ["scala"],
  "allowedVersions": "/^(2\\.13\\.|3\\.9\\.)/"
}
```

To move to the next LTS: widen the pattern to the new branch, migrate `scalaVersion` and
`crossScalaVersions` in `build.sbt` (and `consumer-smoke/build.sbt`), then narrow the pattern
again. Publishing for a newer LTS is a minor-version decision: consumers on the previous LTS
cannot read the new TASTy.

## Notes

- The archived `sbt-build-seed` plugin is gone; its settings live directly in `build.sbt`.
- Keep `README.md` (usage examples, design decisions) current when the public API or the
  tooling changes.
- `publishTo` is sbt's `localStaging` (Central Portal bundle); `SMOKE_REPOSITORY=<dir>`
  redirects `publish` to a local Maven layout and adds it as a resolver (consumer smoke and
  MiMa wiring proof).
