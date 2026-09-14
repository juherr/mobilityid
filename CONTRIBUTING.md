# Contributing

This repository hosts five implementations of the same mobility ID domain (`scala/`, `java/`,
`go/`, `php/`, `ts/`). `AGENTS.md` at the root and in each workspace is the source of truth for
layout, commands and conventions; this file covers the workflow around a change.

## Prerequisites

Tool versions are pinned in `mise.toml`; run `mise install`. Each workspace is self-contained and
uses its own wrapper or package manager (`./gradlew`, `sbt`, `go`, `composer`, `vp`).

## Making a change

1. Read the workspace `AGENTS.md`, run the narrowest test first, then the workspace full gate.
2. A change to the domain model lands in Scala first (the reference) and is ported to every
   workspace with the same test cases, in the same pull request or a linked one.
3. Format before committing (`spotlessApply`, `gofmt`, `composer format`, `bun run format`).
4. Verification entry points: `java/scripts/verify.sh` runs the Java gates, the release-guard check
   and the isolated consumer smoke; `scripts/tests/run.sh` tests the release scripts; other
   workspaces expose their full gate in their `AGENTS.md`.
5. Update the documentation that describes what you changed (`README.md` of the workspace,
   `AGENTS.md` when commands or gates change) and add a line to `CHANGELOG.md` under `Unreleased`.

## Commit messages

[Conventional Commits 1.0.0](https://www.conventionalcommits.org/en/v1.0.0/): `type(scope):
description` with lowercase types (`feat`, `fix`, `docs`, `test`, `refactor`, `build`, `ci`,
`chore`) and the workspace as scope (`java`, `scala`, `go`, `php`, `ts`, `agents`). Mark breaking
changes with `!` after the scope and a `BREAKING CHANGE:` footer with migration guidance. Commit
messages, code, comments and documentation are written in English.

## Pull requests

- One workspace per pull request unless the change is a domain change that must land everywhere.
- CI runs the workflow of each touched workspace plus `CI Workflows` (actionlint, zizmor) when a
  workflow changes. Keep every job green; do not merge on a red job.
- Codecov comments the diff coverage on pull requests (flag per workspace, `codecov.yml`). It is
  informational: the hard coverage gates live in each workspace's own check. Uploads need the
  `CODECOV_TOKEN` repository secret (forks upload tokenless).
- Reference the issue (`Closes #N`) and describe what a reviewer should verify.

## Changelog and release notes

`CHANGELOG.md` follows [Common Changelog](https://common-changelog.org): grouped by impact
(`Changed`, `Added`, `Removed`, `Fixed`), written for humans, breaking changes marked
**Breaking**, no raw commit dumps. Each released version also gets a narrative note in
`.github/release-notes/X.Y.Z.md` (highlights, install snippets, migration notes) that becomes the
GitHub Release body.

## Release workflow

Java (Maven Central) and TypeScript (npm) are released together by the manually dispatched
`Release` workflow; Go is released from `go/vX.Y.Z` tags by `Release Go`.

1. Open and merge a release PR that turns the `Unreleased` section into `## [X.Y.Z] - YYYY-MM-DD`
   and adds `.github/release-notes/X.Y.Z.md`.
2. Dispatch `Release` from `main` with `version = X.Y.Z`. It validates the version with
   `scripts/validate-release-version.sh` (strict SemVer, no `v`, no build metadata, no
   SNAPSHOT: the same rules for Maven Central, npm and the git tag), then runs the preflights:
   Java (`java/scripts/verify.sh`) and TypeScript (`bun run check`, `npm pack`, tarball checked
   by `scripts/verify-npm-package.sh` and uploaded as an artifact), each also asserting that
   its registry credentials are present. Only when **both** pass does it publish, idempotently
   (an already published version is skipped): Java through nmcp, TypeScript by publishing the
   exact verified tarball. It then waits until Maven Central resolves the artifacts and creates
   the signed `vX.Y.Z` tag and the GitHub Release. A failing preflight, including a missing
   secret, leaves every registry untouched.
3. Re-run a failed run with `gh run rerun <run-id> --failed` rather than dispatching again, so the
   tag still points at the commit that produced the published artifacts.

The `maven-central` environment provides `CENTRAL_USERNAME`, `CENTRAL_TOKEN` (a Central Portal
user token), `GPG_PRIVATE_KEY` (armored) and `GPG_PASSPHRASE`; the `npm` environment provides
`NPM_TOKEN`. The public GPG key must be available from a public keyserver.
