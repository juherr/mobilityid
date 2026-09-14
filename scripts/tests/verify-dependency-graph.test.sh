#!/usr/bin/env bash
# Exercises scripts/verify-dependency-graph.sh with synthetic snapshots: a graph carrying the
# resolved transitive dependencies passes, one that only lists declared coordinates (what a
# manifest-only graph would contain), one for another workspace or a missing file fails.
set -euo pipefail

cd "$(dirname "$0")/../.."
script=scripts/verify-dependency-graph.sh
failures=0
work=$(mktemp -d)
trap 'rm -rf "${work}"' EXIT

make_snapshot() {
  local name=$1 source=$2; shift 2
  local file="${work}/${name}.json"
  local resolved=""
  for entry in "$@"; do
    local coordinates=${entry%%=*} relationship=${entry##*=}
    resolved+="\"${coordinates}\":{\"package_url\":\"pkg:maven/${coordinates}\",\"relationship\":\"${relationship}\"},"
  done
  cat > "${file}" <<JSON
{"version":0,"sha":"abc","ref":"refs/heads/x","job":{"id":"1","correlator":"t"},
 "manifests":{"t":{"name":"t","file":{"source_location":"${source}"},"resolved":{${resolved%,}}}}}
JSON
  echo "${file}"
}

expect() {
  local expected=$1 label=$2 snapshot=$3 status=0
  "${script}" "${snapshot}" >/dev/null 2>&1 || status=$?
  if [[ "${status}" -ne "${expected}" ]]; then
    echo "FAIL: ${label} -> exit ${status}, expected ${expected}"
    failures=$((failures + 1))
  else
    echo "ok: ${label} -> ${status}"
  fi
}

full=(org.jspecify:jspecify:1.0.1=direct org.opentest4j:opentest4j:1.3.0=indirect
  com.google.guava:guava:33.6.0-jre=indirect org.gradle:gradle-core:9.7.1=direct)
declared_only=(org.jspecify:jspecify:1.0.1=direct org.junit.jupiter:junit-jupiter:6.1.3=direct
  org.gradle:gradle-core:9.7.1=direct)

expect 0 "resolved graph with transitive dependencies" "$(make_snapshot ok java/settings.gradle.kts "${full[@]}")"
expect 1 "declared coordinates only (no transitive dependencies)" "$(make_snapshot declared java/settings.gradle.kts "${declared_only[@]}")"
expect 1 "transitive dependency reported as direct" "$(make_snapshot wrongrel java/settings.gradle.kts org.jspecify:jspecify:1.0.1=direct org.opentest4j:opentest4j:1.3.0=direct com.google.guava:guava:33.6.0-jre=direct org.gradle:gradle-core:9.7.1=direct)"
expect 1 "graph generated for another workspace" "$(make_snapshot other ts/package.json "${full[@]}")"
expect 1 "snapshot does not exist" "${work}/missing.json"

if (( failures > 0 )); then
  echo "${failures} verify-dependency-graph check(s) failed"
  exit 1
fi
echo "all verify-dependency-graph checks passed"
