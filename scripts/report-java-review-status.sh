#!/usr/bin/env bash
# Publishes the `Java dependency review` commit status on a pull request head commit. The check
# runs of a `workflow_run` workflow are attached to the default-branch commit, not to the pull
# request, so `fork-dependency-graph.yml` reports its outcome through this status for fork and
# Dependabot pull requests; `dependency-submission.yml` publishes the same context for
# same-repository pull requests, so the context can be required on `main` for every pull request.
# Needs `GH_TOKEN` with `statuses: write` and `GITHUB_REPOSITORY`.
# Usage: report-java-review-status.sh <sha> <pending|success|failure|error> <target-url>
set -euo pipefail

usage() {
  echo "Usage: $0 <sha> <pending|success|failure|error> <target-url>" >&2
  exit 2
}

[[ $# -eq 3 ]] || usage
sha=$1 state=$2 target_url=$3

case "${state}" in
  pending) description="Validating and submitting the Java dependency graph" ;;
  success) description="No newly introduced high/critical vulnerability in the Java graph" ;;
  failure) description="Java dependency graph rejected or vulnerable dependency introduced" ;;
  error) description="Java dependency review could not run" ;;
  *) usage ;;
esac

gh api --method POST "repos/${GITHUB_REPOSITORY}/statuses/${sha}" \
  -f "state=${state}" \
  -f "context=Java dependency review" \
  -f "description=${description}" \
  -f "target_url=${target_url}" > /dev/null
echo "Java dependency review: ${state} reported on ${sha}"
