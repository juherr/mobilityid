#!/usr/bin/env bash
# Consumes the published modules from a separate sbt build (consumer-smoke) on every Scala
# version, the way a downstream project would. Reuses the artifacts verify-release-wiring.sh
# left in target/smoke-repo, or publishes them there first when run on its own.
set -euo pipefail

cd "$(dirname "$0")/.."
# shellcheck source=scala/scripts/artifacts.sh
source scripts/artifacts.sh

repository="$(pwd)/target/smoke-repo"
if ! require_published_payloads "${repository}" "${smoke_version}" 2>/dev/null; then
  rm -rf "${repository}"
  mkdir -p "${repository}"
  SMOKE_REPOSITORY="${repository}" RELEASE_VERSION="${smoke_version}" sbt --server --batch "+publish"
  require_published_payloads "${repository}" "${smoke_version}"
fi

cd consumer-smoke
SMOKE_REPOSITORY="${repository}" MOBILITYID_VERSION="${smoke_version}" sbt --server --batch "+run"
