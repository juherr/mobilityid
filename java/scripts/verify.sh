#!/usr/bin/env bash
# Single entry point before opening a PR: quality gates, release artifacts, release guard wiring
# and the isolated consumer smoke. Log in build/verification/verify.log.
set -euo pipefail

cd "$(dirname "$0")/.."
mkdir -p build/verification
log="build/verification/verify.log"
: > "${log}"

run() {
  echo "==> $*" | tee -a "${log}"
  "$@" >>"${log}" 2>&1 || { echo "failed: $* (see ${log})" >&2; exit 1; }
}

run ./gradlew --quiet check javadocJar sourcesJar publishToMavenLocal
run scripts/verify-release-wiring.sh
run scripts/verify-consumer.sh

python3 - <<'PY' | tee -a "${log}"
import xml.etree.ElementTree as ET
root = ET.parse("build/reports/jacoco/test/jacocoTestReport.xml").getroot()
for counter in root.findall("counter"):
    missed, covered = int(counter.get("missed")), int(counter.get("covered"))
    print(f"coverage {counter.get('type').lower():<12} {covered / (missed + covered):.1%}")
PY
echo "verification OK"
