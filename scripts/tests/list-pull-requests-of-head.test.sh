#!/usr/bin/env bash
# Exercises scripts/list-pull-requests-of-head.sh with synthetic pull request listings
# (`GET /repos/{owner}/{repo}/pulls?state=open`, every branch): every open pull request whose
# head is exactly the expected sha is listed with its base, siblings included, whatever their
# head branch or head repository, because the commit status their review feeds is shared by all
# of them (a commit is the same object whichever fork pushed it). The script deliberately takes
# no pull request numbers, branch nor repository: the triggering run's own pull requests and
# head repository (`workflow_run.pull_requests`, `head_repository`) only bind the snapshot
# provenance.
set -euo pipefail

cd "$(dirname "$0")/../.."
script=scripts/list-pull-requests-of-head.sh
failures=0
work=$(mktemp -d)
trap 'rm -rf "${work}"' EXIT

head_sha=1111111111111111111111111111111111111111
old_sha=2222222222222222222222222222222222222222
head_repo=juherr/mobilityid
other_fork=someone/mobilityid

# pull <number> <head-sha> <head-repo> <base-sha> [head-branch]
pull() {
  printf '{"number":%s,"state":"open","head":{"ref":"%s","sha":"%s","repo":{"full_name":"%s"}},"base":{"sha":"%s"}}' \
    "$1" "${5:-feature}" "$2" "$3" "$4"
}

# make_listing <file-name> [pull json...]
make_listing() {
  local file="${work}/$1.json"; shift
  local IFS=,
  printf '[%s]\n' "$*" > "${file}"
  echo "${file}"
}

# expect <status> <expected-stdout> <label> <listing>
expect() {
  local expected=$1 stdout=$2 label=$3 listing=$4 status=0 actual
  actual=$("${script}" "${listing}" "${head_sha}" 2>/dev/null) || status=$?
  if [[ "${status}" -ne "${expected}" || "${actual}" != "${stdout}" ]]; then
    echo "FAIL: ${label} -> exit ${status} stdout '${actual}', expected ${expected} '${stdout}'"
    failures=$((failures + 1))
  else
    echo "ok: ${label} -> ${status} '${actual}'"
  fi
}

to_main=$(pull 42 "${head_sha}" "${head_repo}" base-main)
to_release=$(pull 43 "${head_sha}" "${head_repo}" base-release)
stale=$(pull 44 "${old_sha}" "${head_repo}" base-main)
from_fork=$(pull 45 "${head_sha}" "${other_fork}" base-release)
other_branch=$(pull 46 "${head_sha}" "${head_repo}" base-release other-branch)

expect 0 '[{"number":42,"base":"base-main"}]' "single pull request" "$(make_listing single "${to_main}")"
expect 0 '[{"number":42,"base":"base-main"},{"number":43,"base":"base-release"}]' \
  "siblings sharing the head with different bases are all listed" \
  "$(make_listing siblings "${to_main}" "${to_release}")"
expect 0 '[{"number":42,"base":"base-main"},{"number":46,"base":"base-release"}]' \
  "another head branch at the same sha with another base is listed too" \
  "$(make_listing branches "${to_main}" "${other_branch}")"
expect 0 '[{"number":42,"base":"base-main"}]' "pull request whose head moved is ignored" \
  "$(make_listing stale "${to_main}" "${stale}")"
expect 0 '[{"number":42,"base":"base-main"},{"number":45,"base":"base-release"}]' \
  "pull request from another fork at the same sha is listed too (same commit, same status)" \
  "$(make_listing forks "${to_main}" "${from_fork}")"
expect 1 "" "no pull request" "$(make_listing none)"
expect 1 "" "only pull requests whose head moved" "$(make_listing nothing "${stale}")"
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
  echo "${failures} list-pull-requests-of-head check(s) failed"
  exit 1
fi
echo "all list-pull-requests-of-head checks passed"
