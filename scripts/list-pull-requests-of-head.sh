#!/usr/bin/env bash
# Lists the open pull requests whose head is exactly the given repository and sha, with their
# base sha, from a pull request listing (`GET /repos/{owner}/{repo}/pulls?head=<owner>:<branch>
# &state=open`): the review set of trusted-dependency-review.yml, one matrix leg per entry. The
# `Trusted dependency review` commit status is shared by every pull request having that head,
# so the set must hold all of them, siblings with different bases included; it is never reduced
# to the triggering run's own pull requests (those only bind the snapshot provenance).
# Prints a compact JSON array `[{"number":N,"base":"<sha>"},...]` on stdout. Exit 0 when at
# least one pull request is found, 1 otherwise, 2 on usage error.
# Usage: list-pull-requests-of-head.sh <pulls.json> <head-sha> <head-repo>
set -euo pipefail

if [[ $# -ne 3 ]]; then
  echo "Usage: $0 <pulls.json> <head-sha> <head-repo>" >&2
  exit 2
fi

listing=$1 head_sha=$2 head_repo=$3

if [[ ! -f "${listing}" ]]; then
  echo "File not found: ${listing}" >&2
  exit 1
fi

if ! pulls=$(jq -c --arg sha "${head_sha}" --arg repo "${head_repo}" '
  [.[] | select(.state == "open" and .head.sha == $sha and .head.repo.full_name == $repo)
       | {number, base: .base.sha}]' "${listing}" 2>/dev/null); then
  echo "Malformed pull request listing: ${listing}" >&2
  exit 1
fi

if [[ "${pulls}" == "[]" ]]; then
  echo "No open pull request with head ${head_sha} from ${head_repo} in the listing" >&2
  exit 1
fi

echo "Review set: ${pulls}" >&2
echo "${pulls}"
