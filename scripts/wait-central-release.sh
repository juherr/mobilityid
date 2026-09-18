#!/usr/bin/env bash
# Usage: wait-central-release.sh <version> [check-central-release.sh arguments...]
# Polls Maven Central until every payload of the version resolves (exit 0). A transient failure to
# question Central counts as "not yet". Exit 1 when the artifacts are still absent after the last
# attempt (CENTRAL_WAIT_ATTEMPTS, default 60, every CENTRAL_WAIT_SECONDS, default 30).
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <version> [check-central-release.sh arguments...]" >&2
  exit 2
fi

attempts=${CENTRAL_WAIT_ATTEMPTS:-60}
interval=${CENTRAL_WAIT_SECONDS:-30}
check="$(dirname "$0")/check-central-release.sh"

for attempt in $(seq 1 "${attempts}"); do
  if "${check}" "$@"; then
    exit 0
  fi
  if (( attempt < attempts )); then
    sleep "${interval}"
  fi
done
echo "Release $1 did not become resolvable from Maven Central in time." >&2
echo "Check https://central.sonatype.com/publishing/deployments, then re-run this failed job." >&2
exit 1
