#!/usr/bin/env bash
# Proves the license header gate (vite.config.ts, Oxlint header plugin) rejects a file whose
# header deviates from license-header.txt: a missing copyright line and an extra one both fail,
# and the tree as committed passes. The probe edits one file in place and restores it on exit.
set -euo pipefail

cd "$(dirname "$0")/../.."
probe=src/parsers.ts
backup=$(mktemp)
cp "${probe}" "${backup}"
trap 'cp "${backup}" "${probe}"; rm -f "${backup}"' EXIT

failures=0
expect() {
  local expected=$1 label=$2 status=0
  vp lint src test >/dev/null 2>&1 || status=$?
  if [[ "${status}" -ne "${expected}" ]]; then
    echo "FAIL: ${label} -> exit ${status}, expected ${expected}"
    failures=$((failures + 1))
  else
    echo "ok: ${label} -> ${status}"
  fi
}

expect 0 "committed tree"

sed -i.bak '/Copyright (c) 2026 Julien Herr/d' "${probe}" && rm "${probe}.bak"
expect 1 "file without the copyright line"
cp "${backup}" "${probe}"

sed -i.bak 's| \* Copyright (c) 2026 Julien Herr| * Copyright (c) 2014 Someone Else\
 * Copyright (c) 2026 Julien Herr|' "${probe}" && rm "${probe}.bak"
expect 1 "file with an extra copyright line"
cp "${backup}" "${probe}"

if (( failures > 0 )); then
  echo "${failures} header policy check(s) failed"
  exit 1
fi
echo "all header policy checks passed"
