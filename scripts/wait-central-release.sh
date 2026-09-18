#!/usr/bin/env bash
# Usage: wait-central-release.sh <version> [check-central-release.sh arguments...]
# Polls Maven Central until every payload of the version resolves (exit 0). Absent, partially
# visible or unreachable all count as "not yet". Exit 1 when the artifacts are still not fully
# visible after the last attempt (CENTRAL_WAIT_ATTEMPTS, default 120, every CENTRAL_WAIT_SECONDS,
# default 60: the Central Portal documents "10 minutes to a few hours" before repo1 serves a release).
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <version> [check-central-release.sh arguments...]" >&2
  exit 2
fi

attempts=${CENTRAL_WAIT_ATTEMPTS:-120}
interval=${CENTRAL_WAIT_SECONDS:-60}
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
