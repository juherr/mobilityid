#!/usr/bin/env bash
# Publishes both modules for every Scala version to an isolated Maven repository and consumes
# them from a separate sbt build (consumer-smoke), the way a downstream project would.
set -euo pipefail

cd "$(dirname "$0")/.."

version="${1:-0.0.0-smoke}"
repository="$(pwd)/target/smoke-repo"

rm -rf "${repository}"
mkdir -p "${repository}"
SMOKE_REPOSITORY="${repository}" RELEASE_VERSION="${version}" sbt --server --batch "+publish"
for artifact in mobilityid_2.13 mobilityid_3 mobilityid-interpolators_2.13 mobilityid-interpolators_3; do
  base="${repository}/dev/juherr/mobilityid/${artifact}/${version}/${artifact}-${version}"
  for payload in .pom .jar -sources.jar -javadoc.jar; do
    test -f "${base}${payload}" || { echo "missing published payload: ${base}${payload}" >&2; exit 1; }
  done
done

cd consumer-smoke
SMOKE_REPOSITORY="${repository}" MOBILITYID_VERSION="${version}" sbt --server --batch "+run"
