#!/usr/bin/env bash
# Exercises scripts/select-dependency-graph-artifact.sh with synthetic artifact listings
# (`GET /repos/{owner}/{repo}/actions/runs/{run_id}/artifacts?name=...`): among the artifacts of
# the expected name bound to the head sha, the one created last wins (a workflow re-run keeps
# the artifacts of the previous attempts on the same run id); expired or foreign ones are ignored.
set -euo pipefail

cd "$(dirname "$0")/../.."
script=scripts/select-dependency-graph-artifact.sh
failures=0
work=$(mktemp -d)
trap 'rm -rf "${work}"' EXIT

name=dependency-graph_dependency_submission-java-graph.json
head_sha=1111111111111111111111111111111111111111
other_sha=2222222222222222222222222222222222222222

# artifact <id> <name> <created_at> <expired> <head-sha>
artifact() {
  printf '{"id":%s,"name":"%s","created_at":"%s","expired":%s,"workflow_run":{"head_sha":"%s"}}' "$@"
}

# make_listing <file-name> [artifact json...]
make_listing() {
  local file="${work}/$1.json"; shift
  local IFS=,
  printf '{"total_count":%s,"artifacts":[%s]}\n' "$#" "$*" > "${file}"
  echo "${file}"
}

# expect <status> <expected-stdout> <label> <listing>
expect() {
  local expected=$1 stdout=$2 label=$3 listing=$4 status=0 actual
  actual=$("${script}" "${listing}" "${name}" "${head_sha}" 2>/dev/null) || status=$?
  if [[ "${status}" -ne "${expected}" || "${actual}" != "${stdout}" ]]; then
    echo "FAIL: ${label} -> exit ${status} stdout '${actual}', expected ${expected} '${stdout}'"
    failures=$((failures + 1))
  else
    echo "ok: ${label} -> ${status} '${actual}'"
  fi
}

first=$(artifact 101 "${name}" 2026-09-18T10:00:00Z false "${head_sha}")
rerun=$(artifact 202 "${name}" 2026-09-18T11:00:00Z false "${head_sha}")
expired=$(artifact 303 "${name}" 2026-09-18T12:00:00Z true "${head_sha}")
foreign_sha=$(artifact 404 "${name}" 2026-09-18T12:00:00Z false "${other_sha}")
other_name=$(artifact 505 other-artifact.zip 2026-09-18T12:00:00Z false "${head_sha}")

expect 0 101 "single artifact" "$(make_listing single "${first}")"
expect 0 202 "re-run: the artifact created last wins" "$(make_listing rerun "${rerun}" "${first}")"
expect 0 202 "expired artifact of a later attempt is ignored" "$(make_listing expired "${first}" "${rerun}" "${expired}")"
expect 0 101 "artifact bound to another head sha is ignored" "$(make_listing sha "${first}" "${foreign_sha}")"
expect 0 101 "artifact with another name is ignored" "$(make_listing name "${first}" "${other_name}")"
expect 1 "" "no artifact" "$(make_listing none)"
expect 1 "" "only foreign artifacts" "$(make_listing foreign "${expired}" "${foreign_sha}" "${other_name}")"
expect 1 "" "listing does not exist" "${work}/missing.json"
echo 'not json' > "${work}/garbage.json"
expect 1 "" "listing is not JSON" "${work}/garbage.json"

status=0
"${script}" "${work}/single.json" >/dev/null 2>&1 || status=$?
if [[ "${status}" -ne 2 ]]; then
  echo "FAIL: usage error -> exit ${status}, expected 2"
  failures=$((failures + 1))
else
  echo "ok: usage error -> 2"
fi

if (( failures > 0 )); then
  echo "${failures} select-dependency-graph-artifact check(s) failed"
  exit 1
fi
echo "all select-dependency-graph-artifact checks passed"
