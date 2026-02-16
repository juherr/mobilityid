# AGENTS.md

Guidance for coding agents working in this repository.

## Readme map

- Monorepo overview and quick commands: `README.md`.
- Scala/original library documentation and usage examples: `scala/README.md`.
- Java (`mobilityid4j`) documentation and API/tooling choices: `java/README.md`.

## Project Snapshot

- Multi-workspace repository:
  - `scala/` -> legacy/primary Scala implementation (sbt, specs2).
  - `java/` -> Java 21 port (`mobilityid4j`) using Gradle.
- Scala build tool: sbt (`scala/build.sbt`, `scala/project/build.properties`).
- Scala modules:
  - `scala/core` -> main mobility ID domain logic.
  - `scala/interpolators` -> compile-time checked string interpolators.
  - `scala/` aggregate project.
- Scala versions in CI: 2.12.21, 2.13.18, 3.3.7 and 3.8.1 (`.github/workflows/ci-scala.yml`).
- Scala test framework: specs2 4.14.1-cross (cross-compatible Scala 2.12/2.13/3.x version).
- Java build tool: Gradle wrapper (`java/gradlew`) with Java 21 toolchain.
- Java module:
  - `java` -> domain types, algorithms, and parser helper APIs.

## Repository Layout

- `scala/core/src/main/scala/com/thenewmotion/mobilityid`
  - domain types: `ContractId`, `EvseId`, `PartyId`, operator/provider/country IDs.
  - algorithms: check digit implementations for ISO and DIN.
- `scala/core/src/test/scala/com/thenewmotion/mobilityid`
  - specs2 test suites (`*Spec.scala`).
- `scala/interpolators/src/main/scala-2/com/thenewmotion/mobilityid`
  - Scala 2 interpolator implementations using `contextual-core` macros.
- `scala/interpolators/src/main/scala-3/com/thenewmotion/mobilityid`
  - Scala 3 interpolator implementations using inline macros (`scala.quoted`).
- `scala/interpolators/src/test/scala/com/thenewmotion/mobilityid`
  - interpolator behavior specs (shared across Scala versions).
- `java/src/main/java/dev/juherr/mobilityid4j`
  - Java port of domain types and algorithms.
- `java/src/main/java/dev/juherr/mobilityid4j/interpolators`
  - Java parser helper APIs.

## Build, Lint, and Test Commands

Run from repository root unless noted.

### Core build and test

- Compile all Scala modules:
  - `cd scala && sbt compile`
- Run all Scala tests:
  - `cd scala && sbt test`
- Clean and run all Scala tests (CI-like baseline):
  - `cd scala && sbt clean test`
- Compile and test a specific Scala module:
  - `cd scala && sbt core/test`
  - `cd scala && sbt interpolators/test`

### Run a single test suite (important)

- Single suite in `core`:
  - `cd scala && sbt "core/testOnly com.thenewmotion.mobilityid.ContractIdSpec"`
- Single suite in `interpolators`:
  - `cd scala && sbt "interpolators/testOnly com.thenewmotion.mobilityid.InterpolatorsSpec"`
- Pattern form:
  - `cd scala && sbt "core/testOnly *EvseIdSpec"`

### Run a single test example (specs2)

- Target one example by text fragment:
  - `cd scala && sbt "core/testOnly com.thenewmotion.mobilityid.ContractIdSpec -- -ex 'render a contract id in the normalized form with dashes and check digit'"`
- Use `--` to forward args to specs2.
- Common useful specs2 args:
  - `-ex <example text>` include matching examples.
  - `-x <example text>` exclude matching examples.

### Cross-version and dependency checks

- Test against a specific Scala version:
  - `cd scala && sbt ++2.12.21 test`
  - `cd scala && sbt ++2.13.18 test`
  - `cd scala && sbt ++3.3.7 test`
  - `cd scala && sbt ++3.8.1 test`
- Cross-build tests on all configured Scala versions:
  - `cd scala && sbt +test`
- Update dependency graph and compile check:
  - `cd scala && sbt update compile`

### Java build and test (mobilityid4j)

- Build all Java modules:
  - `cd java && ./gradlew build`
- Run Java tests:
  - `cd java && ./gradlew test`
- Run a single Java suite:
  - `cd java && ./gradlew test --tests "*ContractIdTest"`
- Format and lint checks:
  - `cd java && ./gradlew spotlessApply`
  - `cd java && ./gradlew spotlessCheck`
- Release readiness checks:
  - `cd java && ./gradlew check`
  - `cd java && ./gradlew javadocJar sourcesJar`
  - `cd java && ./gradlew publishToMavenLocal`

### Lint / formatting

- Scala workspace has no repository-local Scalafmt/Scalafix config checked in.
- Scala workspace has no standalone linter config (Scalastyle/Scapegoat) checked in.
- Treat these as guaranteed checks:
  - `cd scala && sbt compile`
  - `cd scala && sbt test`
- Java workspace uses Spotless + Error Prone + NullAway + JSpecify.
- Java publishing metadata/signing is configured for Maven Central Portal workflows in `java/build.gradle.kts`.
- Global release tags `vX.Y.Z` trigger `.github/workflows/release.yml` for target publication pipelines.
- Security scanning runs in CI via dependency review (`.github/workflows/dependency-review.yml`) and OWASP Dependency-Check (`.github/workflows/security.yml`).
- Java CI tests against JDK 21 and JDK 25 (`.github/workflows/ci-java.yml`).
- If your environment exposes additional tasks via plugins, discover first:
  - `cd scala && sbt tasks`

## Agent Workflow Expectations

- Make the smallest safe change that solves the request.
- Keep changes workspace-scoped and module-scoped when possible (`scala/core`, `scala/interpolators`, `java`).
- Run targeted tests first, then broaden to module/all tests as needed.
- Do not refactor broadly unless requested.
- Preserve public API compatibility unless the task explicitly allows breaking changes.
- Keep documentation current while implementing changes:
  - Update `AGENTS.md` whenever workflow, commands, repo layout, or quality gates evolve.
  - Update `README.md`, `scala/README.md`, and `java/README.md` incrementally as features, API choices, or tooling decisions change.
  - Record practical lessons learned (pitfalls, conventions, migration notes) in the relevant README/guide instead of leaving them only in PR/chat context.

## Code Style Guidelines

The Scala codebase targets Scala 2.12, 2.13 and 3.x. Core code is cross-compatible Scala 2/3. The `interpolators` module uses version-specific source directories (`scala-2/` and `scala-3/`) for macro implementations.
For Java (`mobilityid4j`), keep APIs idiomatic and prefer local `var` only when the inferred type is obvious at a glance.

### Formatting and structure

- Use 2-space indentation.
- Keep line length moderate and readable; avoid dense one-liners.
- Prefer small methods and explicit helper names over clever chaining.
- Keep related domain types and their companion objects close together.
- Use braces for multi-line blocks and control structures.

### Imports

- Prefer explicit imports over wildcard imports.
- Group imports by origin:
  1. Scala/JDK imports.
  2. third-party imports.
  3. project-local imports.
- Keep stable ordering and remove unused imports.
- Alias only when collision or readability needs it.

### Naming conventions

- Types and traits: `PascalCase` (`ContractIdParser`, `OperatorIdDin`).
- Objects/vals/defs: `camelCase`.
- Constants with semantic weight may use `val` in `PascalCase` only when matching existing style (for parser regex fields like `FullRegex`).
- Test suites end with `Spec`.
- Prefer descriptive names over abbreviations except established domain abbreviations (ISO, DIN, EMI3, EVSE).

### Types and APIs

- Prefer algebraic modeling with `sealed trait` + private case class implementations.
- Keep constructors/validation centralized in companion `apply` methods.
- Use explicit return types for public methods and important internal methods.
- Use `Option` for parse-or-not cases when absence is expected (`EvseId(string): Option[...]`).
- Throw `IllegalArgumentException` for invalid caller input in strict constructors.
- Use `Try(...).toOption` when providing forgiving parse helpers (`opt` style APIs).

### Validation and normalization

- Normalize external identifiers to uppercase where domain requires it.
- Validate input by regex + semantic checks (country code, lengths, check digits).
- Keep error messages actionable and specific to the failed field.
- Preserve existing behavior of check-digit algorithms unless explicitly changing spec behavior.

### Error handling

- Prefer domain-safe return types (`Option`, `Either`) at parsing boundaries.
- Reserve exceptions for constructor-level invariant violations.
- Do not swallow exceptions silently unless intentionally converting to `Option`/`Either`.
- When combining candidate parsers, keep deterministic error precedence.

### Testing guidelines (specs2)

- Follow existing mutable specs2 style (`class XSpec extends Specification`).
- Describe behavior with nested blocks (`"Subject" should { ... }`).
- Include both positive and negative cases for parsing/validation.
- Verify normalization and rendering (`toString`, compact forms) explicitly.
- Assert thrown exception types/messages where behavior depends on them.
- Add conversion round-trip tests for format conversions when touched.

### Documentation and comments

- Add comments only when intent is non-obvious (algorithmic details, standards mapping).
- Keep public-facing ScalaDoc concise and domain-oriented.
- Do not restate code in comments.

## Change Safety Checklist

Before finalizing a change, an agent should:

1. Run targeted tests for touched suites.
2. Run module tests (`scala/core/test`, `scala/interpolators/test`, `java/test`) when practical.
3. Ensure full workspace tests pass for broad-impact changes (`cd scala && sbt test`, `cd java && ./gradlew test`).
4. Confirm no accidental API/signature changes.
5. Keep thrown error behavior backward compatible unless requested.

## Cursor and Copilot Rules

- Checked for Cursor rules in `.cursor/rules/` and `.cursorrules`: none found.
- Checked for Copilot instructions in `.github/copilot-instructions.md`: none found.
- If any of these files are added later, treat them as higher-priority local agent instructions and merge them into this guide.

## Renovate Configuration Notes

### mise.toml Version Format

The project uses simplified version formats in `mise.toml`:
- **Java**: major only (`openjdk-25`)
- **sbt/gradle**: major.minor (`1.12`, `8.9`)

This is enforced via `extractVersion` in `.github/renovate.json`:

**Default (sbt, gradle):**
```json
"extractVersion": "^(?:openjdk-)?(?<version>\\d+\\.\\d+)"
```

**Java override (packageRule):**
```json
{
  "matchDepNames": ["java"],
  "extractVersion": "^openjdk-(?<version>\\d+)"
}
```

**Consequences:**
- Patch updates (e.g., 1.12.3 → 1.12.4) will NOT trigger PRs for mise.toml
- Minor updates (e.g., 1.12 → 1.13) WILL trigger PRs with correct format
- Build files keep full versions: `scala/project/build.properties` → `sbt.version=1.12.3`
- mise automatically uses the latest patch version available for the specified x.y

### Scala LTS Version Policy

The project follows Scala's LTS (Long-Term Support) strategy:
- **scalaVersion** stays on the current LTS branch (3.3.x as of 2026)
- **crossScalaVersions** includes both LTS (3.3.x) and latest (3.8+)

This is enforced via `allowedVersions` in `.github/renovate.json`:

```json
{
  "matchManagers": ["sbt"],
  "matchPackageNames": ["scala"],
  "allowedVersions": "/^(2\\.|3\\.3\\.|3\\.([89]|[1-9][0-9])\\.)/"
}
```

**Allowed versions:**
- 2.x (Scala 2.12, 2.13)
- 3.3.x (current LTS)
- 3.8, 3.9, 3.10+ (latest branches)

**Blocked versions:**
- 3.4, 3.5, 3.6, 3.7 (non-LTS intermediate releases)

**When the next LTS is announced** (e.g., 3.6.x):
1. Update the pattern to include the new LTS branch: `3\\.6\\.`
2. Migrate `scalaVersion` in `build.sbt` to the new LTS
3. Optionally remove the old LTS (3.3) from the pattern after migration

## Notes for Future Agents

- The Scala workspace no longer depends on the archived `sbt-build-seed` plugin; equivalent core settings are defined directly in `scala/build.sbt`.
- Scala build baseline is `sbt 1.12.3` (`scala/project/build.properties`) with Scala `2.13.18` / `2.12.21` / `3.3.7` / `3.8.1` cross settings in `scala/build.sbt`.
- The `interpolators` module uses version-specific source directories: `scala-2/` (contextual-core macros) and `scala-3/` (inline macros with `scala.quoted`). Shared tests live in `src/test/scala/`.
- When using `beSome.which(...)` in specs2 tests, provide an explicit type parameter (`beSome[T].which(...)`) for Scala 3 type inference compatibility.
- Dependency updates are managed by Renovate (`.github/renovate.json`) for GitHub Actions, Gradle, sbt/Scala, and `mise.toml`.
- For feature parity work, mirror existing behavior from `scala/core` into `java/src/main/java` incrementally.
