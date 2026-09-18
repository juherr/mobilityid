#!/usr/bin/env bash
# Publishes both modules for every Scala version to an isolated Maven repository and consumes
# them from a separate sbt build (consumer-smoke), the way a downstream project would.
set -euo pipefail

cd "$(dirname "$0")/.."
# shellcheck source=scala/scripts/artifacts.sh
source scripts/artifacts.sh

version="${1:-0.0.0-smoke}"
repository="$(pwd)/target/smoke-repo"

rm -rf "${repository}"
mkdir -p "${repository}"
SMOKE_REPOSITORY="${repository}" RELEASE_VERSION="${version}" sbt --server --batch "+publish"
require_published_payloads "${repository}" "${version}"

cd consumer-smoke
SMOKE_REPOSITORY="${repository}" MOBILITYID_VERSION="${version}" sbt --server --batch "+run"
