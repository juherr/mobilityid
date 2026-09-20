#!/usr/bin/env bash
# Lists the open pull requests whose head is exactly the given sha, with their base sha, from a
# pull request listing (`GET /repos/{owner}/{repo}/pulls?state=open`, every branch): the review
# set of trusted-dependency-review.yml, one matrix leg per entry. The `Trusted dependency review`
# commit status is shared by every pull request having that head, whatever its branch or head
# repository (a commit is the same object whichever fork pushed it), so the set must hold all of
# them, siblings with different bases included; it is never reduced to the triggering run's own
# pull requests or repository (those only bind the snapshot provenance).
# Prints a compact JSON array `[{"number":N,"base":"<sha>"},...]` on stdout. Exit 0 when at
# least one pull request is found, 1 otherwise, 2 on usage error.
# Usage: list-pull-requests-of-head.sh <pulls.json> <head-sha>
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <pulls.json> <head-sha>" >&2
  exit 2
fi

listing=$1 head_sha=$2

if [[ ! -f "${listing}" ]]; then
  echo "File not found: ${listing}" >&2
  exit 1
fi

if ! pulls=$(jq -c --arg sha "${head_sha}" '
  [.[] | select(.state == "open" and .head.sha == $sha) | {number, base: .base.sha}]' "${listing}" 2>/dev/null); then
  echo "Malformed pull request listing: ${listing}" >&2
  exit 1
fi

if [[ "${pulls}" == "[]" ]]; then
  echo "No open pull request with head ${head_sha} in the listing" >&2
  exit 1
fi

echo "Review set: ${pulls}" >&2
echo "${pulls}"
