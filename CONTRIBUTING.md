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
- Dependency review: `Dependency review` inside the pull request run is fast feedback on the
  manifest ecosystems; the check to require on `main` is the `Trusted dependency review`
  commit status, see [Dependency review gate](#dependency-review-gate).
- Reference the issue (`Closes #N`) and describe what a reviewer should verify.

## Dependency review gate

GitHub parses the npm, Composer, Go and Actions manifests itself but cannot parse Gradle, so the
resolved Java graph (direct and transitive, tests and build plugins included) has to be
submitted to the Dependency Graph, and pull requests are reviewed against it. Three workflows
share the work; the required check is a commit status set by a dedicated GitHub App.

### What runs where

- `dependency-submission.yml` (`Dependency Submission`). On `push` to `main` (or a dispatch on
  `main`) the `java-graph` job generates and submits the graph (`contents: write`). On every
  `pull_request` the read-only `java-graph-upload` job checks out the **head commit** (not the
  merge commit: the snapshot is recorded for the head sha, which sibling pull requests towards
  different bases share), generates the graph and uploads it as an artifact; the
  `Dependency review` job then reviews the manifest ecosystems and fails when the upload failed.
  That run executes the pull request's own workflow files, so nothing it produces is trusted.
- `trusted-dependency-review.yml` (`Trusted Dependency Review`, `workflow_run` on `Dependency
  Submission`, runs `main`'s definition, checks out nothing but `scripts/` of `main`). On every
  event it first resets the `Trusted dependency review` status to `pending` (job `pending`);
  on `completed` it then submits the uploaded graph once
  `scripts/validate-dependency-graph-provenance.sh` has bound it to the triggering run and its
  pull request (job `submit`; the artifact is selected by
  `scripts/select-dependency-graph-artifact.sh` and extracted under the runner temp directory),
  reviews every open pull request whose head is that commit against its own base (jobs `pulls`
  and `review`, `scripts/list-pull-requests-of-head.sh`, one matrix leg per base) and publishes
  the outcome as the commit status (job `status`, `scripts/report-trusted-review-status.sh`):
  success only when every leg passed, failure otherwise — a run that uploaded nothing or a
  rejected snapshot is a failure.
- `pull-request-lifecycle.yml` (`Pull Request Lifecycle`, `pull_request_target` on `edited`
  with a base change, `reopened` and `closed`; `main`'s definition, no App credential). A pull
  request retargeted, reopened or closed while siblings stay open changes the review set or a
  base without a new commit and without reaching `Dependency Submission`: this workflow re-runs
  a `Dependency Submission` run of the head bound to a pull request still open there
  (`scripts/select-dependency-submission-run.sh`), so the trusted workflow resets and recomputes.

### Trust model

- In scope: pull request **content** — fork authors, Dependabot and Renovate updates, and
  whatever the build executes when the graph is generated (dependencies and Gradle plugins
  resolved by the pull request, in a read-only job). Such content can hide a dependency from
  its own pull request's review by editing the build files; it cannot forge the status, the
  graph of another commit, or `main`'s graph.
- Out of scope: collaborators with **write access**. GitHub's Dependency Graph is writable by
  anyone holding `contents: write` (a personal token and one `POST /dependency-graph/snapshots`
  submit any snapshot for any commit), and such a collaborator can grant that scope to their
  own `pull_request` run by editing `permissions:`. No repository-side design can make the
  compared graph immutable for them; they are trusted, as they are for Dependabot alerts.
- Why a commit status set by a dedicated App: a required check is matched by context name
  and, at best, by the app that set it; check runs of jobs and statuses set with
  `GITHUB_TOKEN` all come from the "GitHub Actions" source, which a pull request's own
  workflow can produce, and check runs of a `workflow_run` workflow are attached to the `main`
  commit, not to the pull request. The App key is a secret of the `trusted-review`
  **environment**, restricted to the `main` branch: an environment secret is only handed to
  jobs whose run ref is `main` (`push`/`workflow_dispatch` on `main`, `workflow_run`,
  `pull_request_target`, all executing `main`'s workflow files), never to a `pull_request` run.
  Consequently no workflow on `main` checks out, downloads or executes pull request content in
  a job holding that key.
- Why the status is per commit: it is shared by every pull request having that head, whatever
  its branch or fork, so the review set is every open pull request at that sha, the
  concurrency group is the sha, and a success already on the commit (another pull request,
  another base, an earlier attempt) is reset to `pending` before any recomputation — as early
  as the `requested`/`in_progress` events (a re-run emits no `requested`) and again as the first
  job of the `completed` run, on which every other job depends. What remains is the few seconds
  before that `pending` lands: keep a required approval on `main` (a new pull request has none)
  and do not rely on auto-merge alone.
- Why the provenance check: the artifact is pull request content. It is accepted only when
  `sha` is the head of the triggering run, `ref` is `refs/pull/<N>/merge`, `job.id` and
  `job.correlator` are the triggering run's, pull request N has that head from the run's head
  repository and is one of the run's pull requests when the run knows them
  (`workflow_run.pull_requests`, empty for forks, so a fork snapshot may name a sibling of the
  same fork on the same commit — snapshots are keyed by sha, `ref` is metadata; the pull
  request's state is not provenance either, so a closed sibling still binds it), and every
  manifest is a canonical path under `java/`. The `download-and-submit` mode of `gradle/actions`
  submits the artifact verbatim and is therefore not used. On a re-run the artifacts of the
  previous attempts stay on the run: the most recent one is selected.
- The result depends on the base at review time while the status only depends on the head:
  "Require branches to be up to date before merging" is a **prerequisite**, it forces a new
  head commit, hence a recomputation, whenever the base branch moved.
- `workflow_run` and `pull_request_target` workflows only run from `main`, so changes to those
  files take effect after merge.

### Setup, in this order

1. Create the `trusted-review` environment with deployment branch policy "Selected branches" =
   `main` (referencing a missing environment would create it without any policy).
2. Create a GitHub App (any name, e.g. `mobilityid-dependency-review`; permissions:
   Repository → Commit statuses: Read and write, nothing else; no webhook) and install it on
   this repository only.
3. Add to that environment the variable `DEPENDENCY_REVIEW_APP_CLIENT_ID` (App client id) and
   the secret `DEPENDENCY_REVIEW_APP_PRIVATE_KEY` (a private key of the App, PEM). While they
   are unset every `Trusted Dependency Review` run fails at its first job and no status is
   published.
4. Allow `pull_request_target` for `pull-request-lifecycle.yml`. GitHub's workflow execution
   protections (generally available since 2026-09-17) give public repositories without an
   applicable Actions event policy a default rule that disables `pull_request_target`, in
   evaluate mode first and enforced from 2026-11-02. In Settings → Actions → Policies (or the
   organization's), add an event rule that allows `pull_request_target`, targeted at
   `.github/workflows/pull-request-lifecycle.yml` only (workflow file targeting), then check
   the policy insights: a `Pull Request Lifecycle` run must evaluate as allowed.
5. After the checks below pass, on `main`: require the status check `Trusted dependency review`
   with the App as its source (offered once it has set the status at least once), enable
   "Require branches to be up to date before merging" and keep a required approval.

### Post-merge checks

Red, from a fork: a pull request that renames the `Dependency Submission` workflow and adds a
green job named `Trusted dependency review` must stay blocked (the forged job is listed under
the GitHub Actions source, the App status never appears). Red, from a same-repository branch: a
job with `environment: trusted-review` must fail on the environment rule, a job without it must
see the secret empty; a pull request that keeps the `Dependency Submission` name but removes
the upload job, or uploads a snapshot with another sha or ref, must get a failed status. Green:
a normal pull request, fork and same-repository alike, gets the App status. Siblings: several
pull requests at one sha (same branch towards different bases, another branch, another fork)
must each get a review leg, one status, identical uploaded snapshots; closing a failing sibling
must recompute the remaining ones. Stale success: a new pull request on a green commit, a
re-run of `Dependency Submission`, or a retarget of an unchanged head must each turn the status
`pending` before any review job starts, then reflect the recomputed reviews. Base moved: a push
to `main` while a green pull request is open must require updating the branch. Artifact: an
upload packing extra files with unexpected paths must leave the trusted scripts untouched.

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
