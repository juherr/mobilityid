#!/usr/bin/env bash
# Publishes the `Trusted dependency review` commit status on a pull request head commit. The
# check runs of a `workflow_run` workflow are attached to the default-branch commit, not to the
# pull request, so `trusted-dependency-review.yml` reports its outcome through this status.
# `GH_TOKEN` must be an installation token of the dedicated GitHub App (`statuses: write`), not
# GITHUB_TOKEN: the required check is restricted to that App because any `pull_request` workflow
# (a fork's or a same-repository branch's) can create a same-named check under the
# "GitHub Actions" source. Needs `GITHUB_REPOSITORY`.
# Usage: report-trusted-review-status.sh <sha> <pending|success|failure|error> <target-url>
set -euo pipefail

usage() {
  echo "Usage: $0 <sha> <pending|success|failure|error> <target-url>" >&2
  exit 2
}

[[ $# -eq 3 ]] || usage
sha=$1 state=$2 target_url=$3

case "${state}" in
  pending) description="Reviewing the dependency changes from the default branch" ;;
  success) description="No newly introduced high/critical vulnerability (Java graph included)" ;;
  failure) description="Dependency graph rejected or vulnerable dependency introduced" ;;
  error) description="Trusted dependency review could not run" ;;
  *) usage ;;
esac

gh api --method POST "repos/${GITHUB_REPOSITORY}/statuses/${sha}" \
  -f "state=${state}" \
  -f "context=Trusted dependency review" \
  -f "description=${description}" \
  -f "target_url=${target_url}" > /dev/null
echo "Trusted dependency review: ${state} reported on ${sha}"
