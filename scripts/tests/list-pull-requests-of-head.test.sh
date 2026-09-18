#!/usr/bin/env bash
# Exercises scripts/list-pull-requests-of-head.sh with synthetic pull request listings
# (`GET /repos/{owner}/{repo}/pulls?head=<owner>:<branch>&state=open`): every open pull request
# whose head is exactly the expected repository and sha is listed with its base, siblings
# included, because the commit status their review feeds is shared by all of them. The script
# deliberately takes no pull request numbers: the triggering run's own pull requests
# (`workflow_run.pull_requests`) only bind the snapshot provenance, never the review set.
set -euo pipefail

cd "$(dirname "$0")/../.."
script=scripts/list-pull-requests-of-head.sh
failures=0
work=$(mktemp -d)
trap 'rm -rf "${work}"' EXIT

head_sha=1111111111111111111111111111111111111111
old_sha=2222222222222222222222222222222222222222
head_repo=juherr/mobilityid

# pull <number> <head-sha> <head-repo> <base-sha>
pull() {
  printf '{"number":%s,"state":"open","head":{"sha":"%s","repo":{"full_name":"%s"}},"base":{"sha":"%s"}}' "$@"
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
  actual=$("${script}" "${listing}" "${head_sha}" "${head_repo}" 2>/dev/null) || status=$?
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
foreign=$(pull 45 "${head_sha}" someone/else base-main)

expect 0 '[{"number":42,"base":"base-main"}]' "single pull request" "$(make_listing single "${to_main}")"
expect 0 '[{"number":42,"base":"base-main"},{"number":43,"base":"base-release"}]' \
  "siblings sharing the head with different bases are all listed" \
  "$(make_listing siblings "${to_main}" "${to_release}")"
expect 0 '[{"number":42,"base":"base-main"}]' "pull request whose head moved is ignored" \
  "$(make_listing stale "${to_main}" "${stale}")"
expect 0 '[{"number":42,"base":"base-main"}]' "pull request from another head repository is ignored" \
  "$(make_listing foreign "${to_main}" "${foreign}")"
expect 1 "" "no pull request" "$(make_listing none)"
expect 1 "" "only stale or foreign pull requests" "$(make_listing nothing "${stale}" "${foreign}")"
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
