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
  would satisfy a required check). That job is fast feedback inside the pull request run; the
  check to require on `main` is the `Trusted dependency review` status below.
- Pull requests from forks and from Dependabot have a read-only `GITHUB_TOKEN`, so their
  `Dependency review` only covers the manifest ecosystems and their Java graph is uploaded as a
  workflow artifact instead. `Trusted Dependency Review` (`trusted-dependency-review.yml`, a
  privileged `workflow_run` that checks out nothing but `scripts/` of `main`) downloads that
  artifact and submits it only when `scripts/validate-dependency-graph-provenance.sh` binds it to
  the triggering run and its open pull request (checks listed in that script). The
  `download-and-submit` pattern of `gradle/actions` is not used because it submits the artifact
  verbatim, letting a fork forge a snapshot for `main`. On a re-run, the artifacts of the
  previous attempts stay on the run: the most recently created one is selected
  (`scripts/select-dependency-graph-artifact.sh`), so a re-run that does not re-run `Java
  dependency graph` submits the graph of the previous attempt, for the same commit.
  Several open pull requests can share one head (same branch, different bases): the snapshot's
  `refs/pull/<N>/merge` must name one of the pull requests the triggering run belongs to
  (`workflow_run.pull_requests`, populated for same-repository and Dependabot runs); GitHub
  leaves that list empty for fork runs, so a fork snapshot may name any open sibling of the same
  fork on the same commit, which changes neither what it describes nor the commit it is attached
  to (snapshots are keyed by sha, `ref` is metadata).
- **Trust model of the required check.** The pull request author, fork or same-repository
  branch, controls the workflow files a `pull_request` run executes, the build files the graph
  is generated from, and every check or `GITHUB_TOKEN` status that run produces (all under the
  "GitHub Actions" source, which a required check cannot tell apart from a forged one: it is
  matched by context name and, at best, by app). So `Trusted Dependency Review` recomputes the
  review for **every** pull request from `main`'s definition (`actions/dependency-review-action`
  on base...head, all ecosystems, same thresholds as `Dependency review`) and publishes it as the
  `Trusted dependency review` commit status on the pull request head. A commit status is per
  commit, so when several open pull requests share that head the review runs against each of
  their bases (job `pulls` lists them, restricted to the triggering run's own pull requests when
  it knows them; one matrix leg per base) and the status is green only when every leg passes.
  The status is published by `scripts/report-trusted-review-status.sh` because check runs of a
  `workflow_run` workflow are attached to the `main` commit, not to the pull request; it is set
  by a dedicated GitHub App
  whose key is a secret of the `trusted-review` **environment**, restricted to the `main` branch:
  a repository secret would be readable by any same-repository branch adding a workflow, an
  environment secret is only handed to jobs whose run ref passes the branch policy, and a
  `pull_request` run never runs as `main` (its job would fail with "Branch ... is not allowed to
  deploy to trusted-review"), so no pull request can mint that token. What remains under the
  author's control is the graph content (a pull request can hide a dependency from its own
  review by editing the build), limited to that pull request. `workflow_run` workflows only run
  from `main`, so changes to that file take effect after merge.
  - Setup, in this order: create the `trusted-review` environment with deployment branch policy
    "Selected branches" = `main` (referencing a missing environment would create it without any
    policy); create a GitHub App (any name, e.g. `mobilityid-dependency-review`; permissions:
    Repository → Commit statuses: Read and write, nothing else; no webhook) and install it on
    this repository only; then add to that environment the variable
    `DEPENDENCY_REVIEW_APP_CLIENT_ID` (App client id) and the secret
    `DEPENDENCY_REVIEW_APP_PRIVATE_KEY` (a private key of the App, PEM). While they are unset the
    workflow skips the status with a warning; nothing else changes.
  - Required check: on `main`, require the status check `Trusted dependency review` and pick the
    App as its source (offered once it has set the status at least once). Do this only after the
    checks below pass.
  - Adversarial checks, after merge. Red, from a fork: a pull request that renames the
    `Dependency Submission` workflow and adds a job named `Trusted dependency review` that just
    succeeds must stay blocked, the forged job being listed under the GitHub Actions source and
    the App status never appearing. Red, from a same-repository branch: a pull request adding a
    workflow job with `environment: trusted-review` that reads the App secret must fail on the
    environment protection rule before running, and a job without the environment must see the
    secret empty. Green: a normal pull request, fork and same-repository alike, gets the App
    status once `Trusted Dependency Review` has run (for a fork, after the artifact was selected,
    validated and submitted); re-run that workflow once to see the latest artifact selected.
- Reference the issue (`Closes #N`) and describe what a reviewer should verify.

## Changelog and release notes

`CHANGELOG.md` follows [Common Changelog](https://common-changelog.org): grouped by impact
(`Changed`, `Added`, `Removed`, `Fixed`), written for humans, breaking changes marked
**Breaking**, no raw commit dumps. Each released version also gets a narrative note in
`.github/release-notes/X.Y.Z.md` (highlights, install snippets, migration notes) that becomes the
GitHub Release body.

## Release workflow

Java and Scala (Maven Central), TypeScript (npm) and PHP (Packagist, through a split mirror; see
"PHP and Packagist" below) are released together by the manually dispatched `Release` workflow;
Go is released from `go/vX.Y.Z` tags by `Release Go`.

1. Open and merge a release PR that turns the `Unreleased` section into `## [X.Y.Z] - YYYY-MM-DD`
   and adds `.github/release-notes/X.Y.Z.md`.
2. Dispatch `Release` from `main` with `version = X.Y.Z`. It validates the version with
   `scripts/validate-release-version.sh` (strict SemVer, no `v`, no build metadata, no
   SNAPSHOT: the same rules for Maven Central, npm and the git tag), then runs the preflights:
   Java (`java/scripts/verify.sh`, also asserting the Maven Central credentials are present),
   TypeScript (`ts/scripts/verify-package.sh`: build, `npm pack`, tarball content, publint,
   throw-away consumer; the tarball is uploaded as an artifact), PHP (`composer check`, also
   asserting the mirror deploy key is present) and Scala (`scala/scripts/verify.sh`: the full
   gate on Scala 2.13 and 3, the release guard wiring proof and the consumer smoke, also
   asserting the Maven Central credentials are present). Only when **all four** pass does it
   publish, idempotently (an already published version is skipped): Java through nmcp, Scala
   through sbt's Central Portal support (`+publishSigned` stages both Scala versions,
   `sonaRelease` uploads the bundle; the `verifyRelease` guard runs first), TypeScript by
   publishing the exact verified tarball with OIDC trusted publishing, PHP by pushing the
   `php/` split to the Packagist mirror as `vX.Y.Z`. It then waits until Maven Central resolves
   the Java and Scala artifacts and creates the signed `vX.Y.Z` tag and the GitHub Release. A
   failing preflight, including a missing secret, leaves every registry untouched.
3. Re-run a failed run with `gh run rerun <run-id> --failed` rather than dispatching again, so the
   tag still points at the commit that produced the published artifacts. The Maven Central jobs
   query repo1 first (`scripts/check-central-release.sh`): a version whose every payload is
   visible is skipped, one with no payload at all is uploaded, one that is only partially
   visible (a previous upload still propagating) is waited for and never uploaded again, and an
   unreachable Central refuses to upload. Propagation to repo1 can take from ten minutes to a
   few hours: `scripts/wait-central-release.sh` polls for up to two hours before failing the job,
   and a re-run after that simply resumes the wait.

The `maven-central` environment provides `CENTRAL_USERNAME`, `CENTRAL_TOKEN` (a Central Portal
user token), `GPG_PRIVATE_KEY` (armored) and `GPG_PASSPHRASE`, shared by the Java and Scala
jobs (Scala maps them to `SONATYPE_USERNAME`, `SONATYPE_PASSWORD` and `PGP_PASSPHRASE`); the
public GPG key must be available from a public keyserver. After the first Scala release, the
Scala CI and preflight compare the public API with that release (MiMa, baseline read from
Maven Central by `scripts/mima-baseline.sh`); a deliberate break needs a
`mimaBinaryIssueFilters` entry and a major/minor bump. The `npm` environment holds **no secret**: npm is published
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
