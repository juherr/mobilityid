#!/usr/bin/env bash
# Exercises scripts/validate-dependency-graph-provenance.sh with synthetic snapshots and pull
# request lists: only a snapshot bound to the head commit, the run and the open pull request of
# that head is accepted; every forged or stale binding is rejected.
set -euo pipefail

cd "$(dirname "$0")/../.."
script=scripts/validate-dependency-graph-provenance.sh
failures=0
work=$(mktemp -d)
trap 'rm -rf "${work}"' EXIT

# The trusted facts of the triggering run, as the workflow reads them from the workflow_run event.
head_sha=1111111111111111111111111111111111111111
main_sha=2222222222222222222222222222222222222222
base_sha=3333333333333333333333333333333333333333
run_id=987654321
correlator=dependency_submission-java-graph
head_repo=contributor/mobilityid

# make_snapshot <name> <sha> <ref> <run-id> <correlator> [manifest source_location...]
make_snapshot() {
  local file="${work}/$1.json" manifests=""
  for source in "${@:6}"; do
    manifests+="\"${source}\":{\"name\":\"${source}\",\"file\":{\"source_location\":\"${source}\"},\"resolved\":{}},"
  done
  cat > "${file}" <<JSON
{"version":0,"sha":"$2","ref":"$3","job":{"id":"$4","correlator":"$5"},"manifests":{${manifests%,}}}
JSON
  echo "${file}"
}

# make_pulls <name> <state> <head-sha> <head-repo>: the open pull requests of the head branch (#42)
make_pulls() {
  local file="${work}/$1.json"
  cat > "${file}" <<JSON
[{"number":42,"state":"$2","head":{"sha":"$3","repo":{"full_name":"$4"}},"base":{"sha":"${base_sha}"}}]
JSON
  echo "${file}"
}

valid_snapshot=$(make_snapshot valid "${head_sha}" refs/pull/42/merge "${run_id}" "${correlator}" java/settings.gradle.kts)
open_pull=$(make_pulls open open "${head_sha}" "${head_repo}")

# expect <status> <label> <snapshot> [<pulls>]: runs the script against the trusted facts above.
expect() {
  local expected=$1 label=$2 snapshot=$3 pulls=${4:-${open_pull}} status=0
  "${script}" "${snapshot}" "${pulls}" "${head_sha}" "${run_id}" "${correlator}" "${head_repo}" \
    >"${work}/stdout" 2>/dev/null || status=$?
  if [[ "${status}" -ne "${expected}" ]]; then
    echo "FAIL: ${label} -> exit ${status}, expected ${expected}"
    failures=$((failures + 1))
  else
    echo "ok: ${label} -> ${status}"
  fi
}

expect 0 "snapshot bound to the open pull request of the head commit" "${valid_snapshot}"
if grep -qx "base_sha=${base_sha}" "${work}/stdout"; then
  echo "ok: valid snapshot prints base_sha"
else
  echo "FAIL: valid snapshot must print base_sha, got: $(cat "${work}/stdout")"
  failures=$((failures + 1))
fi
expect 0 "several Java manifests" \
  "$(make_snapshot javaonly "${head_sha}" refs/pull/42/merge "${run_id}" "${correlator}" java/settings.gradle.kts java/build.gradle.kts)"

expect 1 "snapshot forged for the default branch (sha and ref of main)" \
  "$(make_snapshot forged "${main_sha}" refs/heads/main "${run_id}" "${correlator}" java/settings.gradle.kts)"
expect 1 "ref of the default branch with the head sha" \
  "$(make_snapshot mainref "${head_sha}" refs/heads/main "${run_id}" "${correlator}" java/settings.gradle.kts)"
expect 1 "ref of another pull request" \
  "$(make_snapshot otherpr "${head_sha}" refs/pull/7/merge "${run_id}" "${correlator}" java/settings.gradle.kts)"
expect 1 "job correlator mismatch" \
  "$(make_snapshot correlator "${head_sha}" refs/pull/42/merge "${run_id}" other-job java/settings.gradle.kts)"
expect 1 "job id is another run" \
  "$(make_snapshot runid "${head_sha}" refs/pull/42/merge 123 "${correlator}" java/settings.gradle.kts)"
expect 1 "manifest outside java/ (shadowing a manifest ecosystem)" \
  "$(make_snapshot shadow "${head_sha}" refs/pull/42/merge "${run_id}" "${correlator}" java/settings.gradle.kts ts/package.json)"
expect 1 "manifest escaping java/ through a parent segment" \
  "$(make_snapshot traversal "${head_sha}" refs/pull/42/merge "${run_id}" "${correlator}" java/../ts/package.json)"
expect 1 "manifest with an absolute path" \
  "$(make_snapshot absolute "${head_sha}" refs/pull/42/merge "${run_id}" "${correlator}" /java/build.gradle.kts)"
expect 1 "manifest with a non-canonical path" \
  "$(make_snapshot noncanonical "${head_sha}" refs/pull/42/merge "${run_id}" "${correlator}" java/./build.gradle.kts)"
expect 1 "pull request head moved since the run" \
  "${valid_snapshot}" "$(make_pulls moved open "${main_sha}" "${head_repo}")"
expect 1 "pull request from another head repository" \
  "${valid_snapshot}" "$(make_pulls otherrepo open "${head_sha}" someone/else)"
expect 1 "pull request closed" \
  "${valid_snapshot}" "$(make_pulls closed closed "${head_sha}" "${head_repo}")"
echo '[]' > "${work}/none.json"
expect 1 "no pull request for the head branch" "${valid_snapshot}" "${work}/none.json"
expect 1 "snapshot does not exist" "${work}/missing.json"
echo 'not json' > "${work}/garbage.json"
expect 1 "snapshot is not JSON" "${work}/garbage.json"
echo '{"sha":1,"ref":[],"job":"x","manifests":[]}' > "${work}/shape.json"
expect 1 "snapshot with the wrong shape" "${work}/shape.json"

status=0
"${script}" "${valid_snapshot}" >/dev/null 2>&1 || status=$?
if [[ "${status}" -ne 2 ]]; then
  echo "FAIL: usage error -> exit ${status}, expected 2"
  failures=$((failures + 1))
else
  echo "ok: usage error -> 2"
fi

if (( failures > 0 )); then
  echo "${failures} validate-dependency-graph-provenance check(s) failed"
  exit 1
fi
echo "all validate-dependency-graph-provenance checks passed"
