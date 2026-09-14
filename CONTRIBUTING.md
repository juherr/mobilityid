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
- `Dependency Submission` rejects newly introduced high/critical vulnerabilities in any scope
  (job `Dependency review`). GitHub parses the npm, Composer, Go and Actions manifests itself; the
  Java graph (direct and transitive, tests and build plugins included) is submitted by the first
  job of that workflow for the pull request head and every commit of `main`, and the review job
  only starts once the submission is done, and fails when that submission failed (a skipped job
  would satisfy a required check). `Dependency review` is the check to require on `main`.
- Pull requests from forks and from Dependabot get no Java graph: `GITHUB_TOKEN` is read-only
  there, and the `workflow_run` download-and-submit pattern documented by `gradle/actions` is
  deliberately not used because the privileged job submits the uploaded artifact verbatim
  (`sha`, `ref` and content unchecked), so a fork could submit a forged snapshot for `main`.
  Their Java dependency changes are therefore reviewed on the manifest ecosystems only before
  merge; the push to `main` submits the merged graph and Dependabot alerts report anything
  vulnerable. Do not merge such a pull request that changes `java/gradle/libs.versions.toml`
  without checking the advisories of the new versions by hand.
- Reference the issue (`Closes #N`) and describe what a reviewer should verify.

## Changelog and release notes

`CHANGELOG.md` follows [Common Changelog](https://common-changelog.org): grouped by impact
(`Changed`, `Added`, `Removed`, `Fixed`), written for humans, breaking changes marked
**Breaking**, no raw commit dumps. Each released version also gets a narrative note in
`.github/release-notes/X.Y.Z.md` (highlights, install snippets, migration notes) that becomes the
GitHub Release body.

## Release workflow

Java (Maven Central), TypeScript (npm) and PHP (Packagist, through a split mirror; see "PHP and
Packagist" below) are released together by the manually dispatched `Release` workflow; Go is
released from `go/vX.Y.Z` tags by `Release Go`.

1. Open and merge a release PR that turns the `Unreleased` section into `## [X.Y.Z] - YYYY-MM-DD`
   and adds `.github/release-notes/X.Y.Z.md`.
2. Dispatch `Release` from `main` with `version = X.Y.Z`. It validates the version with
   `scripts/validate-release-version.sh` (strict SemVer, no `v`, no build metadata, no
   SNAPSHOT: the same rules for Maven Central, npm and the git tag), then runs the preflights:
   Java (`java/scripts/verify.sh`, also asserting the Maven Central credentials are present),
   TypeScript (`ts/scripts/verify-package.sh`: build, `npm pack`, tarball content, publint,
   throw-away consumer; the tarball is uploaded as an artifact) and PHP (`composer check`, also
   asserting the mirror deploy key is present). Only when **all three** pass does it publish,
   idempotently (an already published version is skipped): Java through nmcp, TypeScript by
   publishing the exact verified tarball with OIDC trusted publishing, PHP by pushing the
   `php/` split to the Packagist mirror as `vX.Y.Z`. It then waits until Maven Central resolves
   the artifacts and creates the signed `vX.Y.Z` tag and the GitHub Release. A failing
   preflight, including a missing secret, leaves every registry untouched.
3. Re-run a failed run with `gh run rerun <run-id> --failed` rather than dispatching again, so the
   tag still points at the commit that produced the published artifacts.

The `maven-central` environment provides `CENTRAL_USERNAME`, `CENTRAL_TOKEN` (a Central Portal
user token), `GPG_PRIVATE_KEY` (armored) and `GPG_PASSPHRASE`; the public GPG key must be
available from a public keyserver. The `npm` environment holds **no secret**: npm is published
through Trusted Publishing (OIDC) bound to `release.yml` and this environment, after a one-time
manual first publication (`ts/README.md`, "Publishing to npm"). Never add an npm token to the
repository secrets.

### PHP and Packagist

Packagist cannot index a package that lives in a sub-directory, so `php/` is published through a
read-only split repository, `juherr/mobility-id-php`, that Packagist follows.

One-time setup:

1. Create the empty GitHub repository `juherr/mobility-id-php` (public, no initial commit).
2. Generate a dedicated SSH key pair (`ssh-keygen -t ed25519 -N '' -f mobility-id-php-deploy`),
   add the public key as a **deploy key with write access** on `juherr/mobility-id-php`, and
   store the private key as the `PHP_MIRROR_DEPLOY_KEY` secret of the `packagist` environment of
   this repository (the environment holds nothing else).
3. Push a first split by hand from a checkout of `main` so the mirror has a `main` branch:
   `git push git@github.com:juherr/mobility-id-php.git "$(git subtree split --prefix=php HEAD)":refs/heads/main`.
4. Submit `https://github.com/juherr/mobility-id-php` on https://packagist.org/packages/submit and
   enable the GitHub hook on the mirror (Packagist "Settings" page, or the Packagist GitHub App)
   so every pushed tag is picked up automatically.

What the `Release` workflow does for PHP: `Preflight PHP` runs `composer check` on PHP 8.4 and
fails early when `PHP_MIRROR_DEPLOY_KEY` is missing; `Release PHP` computes
`git subtree split --prefix=php` on the released commit and pushes the split commit to the
mirror's `main` and as the `vX.Y.Z` tag. An existing mirror tag that already points at the same
split commit is skipped (re-runs are idempotent); one that points elsewhere fails the run (a
published Composer version is never moved: bump the version instead). After the run, check
`https://packagist.org/packages/juherr/mobility-id` lists the new version; if the hook was not
installed, click "Update" once.
