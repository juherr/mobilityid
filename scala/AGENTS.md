# AGENTS.md (scala workspace)

Scala reference implementation of the mobility ID domain. Shared, cross-workspace guidance
(domain model, parity rule, license header text, release tags) is in the root `AGENTS.md`.
Run every command below from `scala/`.

## Modules

- `core` -> domain types, parsers, check-digit algorithms (`src/main/scala/com/thenewmotion/mobilityid`).
- `interpolators` -> compile-time checked string interpolators; version-specific sources in
  `src/main/scala-2/` (`contextual-core` macros) and `src/main/scala-3/` (inline macros with
  `scala.quoted`), shared specs in `src/test/scala/`.
- Root project aggregates both.

## Toolchain

- sbt 1.12.3 (`project/build.properties`); `scalaVersion` 3.3.7, `crossScalaVersions`
  2.12.21 / 2.13.18 / 3.3.7 / 3.8.1 (`build.sbt`). CI runs all four (`.github/workflows/ci-scala.yml`).
- Test framework: specs2 4.14.1-cross (cross-compatible 2.12/2.13/3.x).
- No Scalafmt/Scalafix/Scalastyle config is checked in; `sbt compile` and `sbt test` are the
  guaranteed checks. Discover extra plugin tasks with `sbt tasks`.

## Commands

- Compile / test everything: `sbt compile`, `sbt test`, `sbt clean test` (CI-like).
- One module: `sbt core/test`, `sbt interpolators/test`.
- One suite: `sbt "core/testOnly com.thenewmotion.mobilityid.ContractIdSpec"`,
  `sbt "interpolators/testOnly com.thenewmotion.mobilityid.InterpolatorsSpec"`, pattern form
  `sbt "core/testOnly *EvseIdSpec"`.
- One specs2 example: `sbt "core/testOnly com.thenewmotion.mobilityid.ContractIdSpec -- -ex 'render a contract id in the normalized form with dashes and check digit'"`
  (`--` forwards args to specs2; `-ex <text>` includes, `-x <text>` excludes).
- One Scala version: `sbt ++2.12.21 test` (same for 2.13.18, 3.3.7, 3.8.1); all versions: `sbt +test`.
- Dependency refresh: `sbt update compile`.
- License headers (sbt-header 5.10.0): `sbt headerCheck` to validate, `sbt headerCreate` to apply;
  CI runs `headerCheck` before tests.

## Code Style

Core code must compile on Scala 2.12, 2.13 and 3.x; keep it cross-compatible.

- 2-space indentation, moderate line length, braces for multi-line blocks, small explicit helpers
  over clever chaining; keep domain types next to their companion objects.
- Explicit imports grouped Scala/JDK, third-party, project-local; remove unused imports
  (`-Wunused:imports` is on); alias only on collision.
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

## Testing (specs2)

- Mutable style: `class XSpec extends Specification`, nested `"Subject" should { ... }` blocks.
- Cover positive and negative parsing cases, normalization and rendering (`toString`, compact
  forms), thrown exception types/messages, and round trips for format conversions.
- Use `beSome[T].which(...)` with an explicit type parameter for Scala 3 inference.

## Scala LTS Version Policy

`scalaVersion` stays on the current LTS branch (3.3.x as of 2026); `crossScalaVersions` carries
both LTS and latest (3.8+). Enforced in `.github/renovate.json`:

```json
{
  "matchManagers": ["sbt"],
  "matchPackageNames": ["scala"],
  "allowedVersions": "/^(2\\.|3\\.3\\.|3\\.([89]|[1-9][0-9])\\.)/"
}
```

Allowed: 2.x, 3.3.x, 3.8+. Blocked: 3.4–3.7 (non-LTS). When the next LTS is announced:
add its branch to the pattern, migrate `scalaVersion` in `build.sbt`, then optionally drop 3.3.

## Notes

- The archived `sbt-build-seed` plugin is gone; its settings live directly in `build.sbt`.
- Keep `README.md` (usage examples) current when the public API changes.
