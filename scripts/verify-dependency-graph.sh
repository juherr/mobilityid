#!/usr/bin/env bash
# Asserts that a GitHub dependency-graph snapshot generated for the Java build (by
# gradle/actions/dependency-submission) contains the resolved Gradle graph, not just the
# declared coordinates: a direct dependency, a transitive test dependency and a transitive
# build-plugin dependency must all be present. Exit 0 when the snapshot is complete, 1 otherwise.
# The probes are stable transitive dependencies of the catalog: update them if JUnit stops
# depending on opentest4j or Error Prone on Guava.
# Usage: verify-dependency-graph.sh <snapshot.json>
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <snapshot.json>" >&2
  exit 2
fi

snapshot=$1

if [[ ! -f "${snapshot}" ]]; then
  echo "Snapshot not found: ${snapshot}" >&2
  exit 1
fi

python3 - "${snapshot}" <<'PY'
import json
import sys

probes = [
    ("direct", "org.jspecify:jspecify", "declared api dependency"),
    ("indirect", "org.opentest4j:opentest4j", "transitive test dependency (via JUnit)"),
    ("indirect", "com.google.guava:guava", "transitive build-plugin dependency (via Error Prone)"),
    ("direct", "org.gradle:gradle-core", "Gradle build tool itself"),
]

with open(sys.argv[1], encoding="utf-8") as handle:
    snapshot = json.load(handle)

manifests = snapshot.get("manifests") or {}
resolved = {}
for manifest in manifests.values():
    source = (manifest.get("file") or {}).get("source_location", "")
    if source.startswith("java/"):
        resolved.update(manifest.get("resolved") or {})

if not resolved:
    print("No manifest with a source_location under java/ in the snapshot", file=sys.stderr)
    sys.exit(1)

failures = 0
for relationship, coordinates, why in probes:
    matches = [
        key
        for key, value in resolved.items()
        if key.startswith(coordinates + ":") and value.get("relationship") == relationship
    ]
    if matches:
        print(f"ok: {relationship} {coordinates} ({why}) -> {', '.join(sorted(matches))}")
    else:
        print(f"MISSING: {relationship} {coordinates} ({why})", file=sys.stderr)
        failures += 1

indirect = sum(1 for value in resolved.values() if value.get("relationship") == "indirect")
print(f"{len(resolved)} resolved packages, {indirect} transitive")
sys.exit(1 if failures else 0)
PY
