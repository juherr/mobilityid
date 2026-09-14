#!/usr/bin/env bash
# Runs every scripts/tests/*.test.sh.
set -euo pipefail
cd "$(dirname "$0")"
status=0
for test in *.test.sh; do
  echo "==> ${test}"
  "./${test}" || status=1
done
exit "${status}"
