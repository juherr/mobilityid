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

# MiMa baseline: the last Scala release on Maven Central; none before the first release.
baseline_status=0
baseline=$(scripts/mima-baseline.sh) || baseline_status=$?
case "${baseline_status}" in
  0) echo "MiMa baseline: ${baseline}" | tee -a "${log}"; mima_option="-Dmobilityid.mimaBaseline=${baseline}" ;;
  1) echo "MiMa baseline: none published yet" | tee -a "${log}"; mima_option="" ;;
  *) echo "Could not resolve the MiMa baseline from Maven Central (exit ${baseline_status})" >&2; exit 1 ;;
esac

run sbt --server --batch ${mima_option:+"${mima_option}"} \
  "+headerCheckAll; +scalafmtCheckAll; scalafmtSbtCheck; +scalafixAll --check; +test; +mimaReportBinaryIssues"
run scripts/verify-release-wiring.sh
run scripts/verify-consumer.sh
echo "verification OK"
