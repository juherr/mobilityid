#!/usr/bin/env bash
# Single entry point before opening a PR: the full quality gate on every Scala version, the
# release guard wiring and the isolated consumer smoke. Log in target/verification/verify.log.
set -euo pipefail

cd "$(dirname "$0")/.."
mkdir -p target/verification
log="target/verification/verify.log"
: > "${log}"

run() {
  echo "==> $*" | tee -a "${log}"
  "$@" >>"${log}" 2>&1 || { echo "failed: $* (see ${log})" >&2; exit 1; }
}

# MiMa baseline: the last Scala release on Maven Central, empty before the first release
# (the script exits 2, and so does this one, when Central cannot be questioned).
MOBILITYID_MIMA_BASELINE=$(../scripts/mima-baseline.sh)
export MOBILITYID_MIMA_BASELINE
echo "MiMa baseline: ${MOBILITYID_MIMA_BASELINE:-none published yet}" | tee -a "${log}"

run sbt --server --batch "+gate"
run scripts/verify-release-wiring.sh
run scripts/verify-consumer.sh
echo "verification OK"
