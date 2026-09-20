#!/usr/bin/env bash
# Exercises scripts/select-dependency-submission-run.sh with synthetic workflow run listings
# (`GET /repos/{owner}/{repo}/actions/workflows/dependency-submission.yml/runs?event=pull_request
# &head_sha=...`) and open pull request listings: the run to re-run when the review set or a
# base changed is the most recent one bound to a pull request that is still open at that head,
# never the run of the pull request that has just been closed. A same-repository run is bound
# through its `pull_requests`; a fork run has none (GitHub leaves it empty) and is bound through
# its head repository, branch and sha.
set -euo pipefail

cd "$(dirname "$0")/../.."
script=scripts/select-dependency-submission-run.sh
failures=0
work=$(mktemp -d)
trap 'rm -rf "${work}"' EXIT

head_sha=1111111111111111111111111111111111111111
repo=juherr/mobilityid
fork=contributor/mobilityid

# run <id> <created_at> <status> <head-repo> <head-branch> [pull-number...]
run() {
  local id=$1 created=$2 status=$3 head_repo=$4 branch=$5 pulls=""
  for number in "${@:6}"; do pulls+="{\"number\":${number}},"; done
  printf '{"id":%s,"created_at":"%s","status":"%s","head_sha":"%s","head_branch":"%s","head_repository":{"full_name":"%s"},"pull_requests":[%s]}' \
    "${id}" "${created}" "${status}" "${head_sha}" "${branch}" "${head_repo}" "${pulls%,}"
}

# pull <number> <head-repo> <head-branch> [head-sha]
pull() {
  printf '{"number":%s,"state":"open","head":{"sha":"%s","ref":"%s","repo":{"full_name":"%s"}}}' \
    "$1" "${4:-${head_sha}}" "$3" "$2"
}

# listing <file-name> <key> [json...]
listing() {
  local file="${work}/$1.json" key=$2; shift 2
  local IFS=,
  if [[ "${key}" == "runs" ]]; then printf '{"workflow_runs":[%s]}\n' "$*" > "${file}"; else printf '[%s]\n' "$*" > "${file}"; fi
  echo "${file}"
}

# expect <status> <expected-stdout> <label> <runs> <pulls>
expect() {
  local expected=$1 stdout=$2 label=$3 runs=$4 pulls=$5 status=0 actual
  actual=$("${script}" "${runs}" "${pulls}" "${head_sha}" 2>/dev/null) || status=$?
  if [[ "${status}" -ne "${expected}" || "${actual}" != "${stdout}" ]]; then
    echo "FAIL: ${label} -> exit ${status} stdout '${actual}', expected ${expected} '${stdout}'"
    failures=$((failures + 1))
  else
    echo "ok: ${label} -> ${status} '${actual}'"
  fi
}

# Same-repository siblings #42 (open) and #43 (just closed): the latest run is #43's.
run_42=$(run 100 2026-09-20T10:00:00Z completed "${repo}" feature 42)
run_43=$(run 200 2026-09-20T11:00:00Z completed "${repo}" feature 43)
open_42=$(pull 42 "${repo}" feature)
open_43=$(pull 43 "${repo}" feature)

expect 0 "100 completed" "latest run belongs to the closed pull request: the open sibling's run is selected" \
  "$(listing closed runs "${run_43}" "${run_42}")" "$(listing open42 pulls "${open_42}")"
expect 0 "200 completed" "several open siblings: the most recent valid run is selected" \
  "$(listing both runs "${run_43}" "${run_42}")" "$(listing open4243 pulls "${open_42}" "${open_43}")"
expect 0 "200 in_progress" "a run still in progress is reported with its status" \
  "$(listing running runs "$(run 200 2026-09-20T11:00:00Z in_progress "${repo}" feature 43)" "${run_42}")" \
  "$(listing open43 pulls "${open_43}")"

# Fork runs carry no pull request: bound through head repository, branch and sha.
fork_run=$(run 300 2026-09-20T12:00:00Z completed "${fork}" topic)
fork_pull=$(pull 7 "${fork}" topic)
expect 0 "300 completed" "fork run matched through head repository, branch and sha" \
  "$(listing fork runs "${fork_run}")" "$(listing fork7 pulls "${fork_pull}")"
expect 1 "" "fork run whose pull request moved to another branch is not selected" \
  "$(listing forkbranch runs "${fork_run}")" "$(listing fork7other pulls "$(pull 7 "${fork}" other-topic)")"
expect 1 "" "fork run whose pull request head moved is not selected" \
  "$(listing forksha runs "${fork_run}")" "$(listing fork7moved pulls "$(pull 7 "${fork}" topic 2222222222222222222222222222222222222222)")"

expect 1 "" "no run bound to an open pull request" \
  "$(listing onlyclosed runs "${run_43}")" "$(listing open42b pulls "${open_42}")"
expect 1 "" "no run at all" "$(listing none runs)" "$(listing open42c pulls "${open_42}")"
expect 1 "" "run listing does not exist" "${work}/missing.json" "$(listing open42d pulls "${open_42}")"
echo 'not json' > "${work}/garbage.json"
expect 1 "" "run listing is not JSON" "${work}/garbage.json" "$(listing open42e pulls "${open_42}")"

status=0
"${script}" "${work}/closed.json" >/dev/null 2>&1 || status=$?
if [[ "${status}" -ne 2 ]]; then
  echo "FAIL: usage error -> exit ${status}, expected 2"
  failures=$((failures + 1))
else
  echo "ok: usage error -> 2"
fi

if (( failures > 0 )); then
  echo "${failures} select-dependency-submission-run check(s) failed"
  exit 1
fi
echo "all select-dependency-submission-run checks passed"
