#!/usr/bin/env bash
# Table-driven check of scripts/validate-release-version.sh against the node-semver rules npm
# applies, so an accepted version can be published to every registry.
set -euo pipefail

cd "$(dirname "$0")/../.."
script=scripts/validate-release-version.sh
failures=0

expect() {
  local expected=$1 version=$2 status=0
  "${script}" "${version}" >/dev/null 2>&1 || status=$?
  if [[ "${status}" -ne "${expected}" ]]; then
    echo "FAIL: '${version}' -> exit ${status}, expected ${expected}"
    failures=$((failures + 1))
  else
    echo "ok: '${version}' -> ${status}"
  fi
}

# accepted
expect 0 "1.2.3"
expect 0 "0.1.0"
expect 0 "10.20.30"
expect 0 "1.2.3-rc.1"
expect 0 "1.2.3-beta"
expect 0 "1.2.3-alpha.0"
expect 0 "1.2.3-0.3.7"
expect 0 "1.2.3-x.7.z.92"

# rejected
expect 1 "v1.2.3"           # tag-style prefix
expect 1 "1.2"              # not three components
expect 1 "1"
expect 1 "01.2.3"           # leading zero
expect 1 "1.02.3"
expect 1 "1.2.03"
expect 1 "1.2.3-rc.01"      # numeric prerelease identifier with leading zero
expect 1 "1.2.3-"           # empty prerelease
expect 1 "1.2.3-rc..1"      # empty prerelease identifier
expect 1 "1.2.3-rc_1"       # invalid character
expect 1 "1.2.3+build.1"    # build metadata: not usable as a Maven version / git tag
expect 1 "1.2.3-SNAPSHOT"   # never released from CI
expect 1 "1.2.3-snapshot"
expect 1 " 1.2.3"
expect 1 "1.2.3 "
expect 1 ""
expect 1 "1.2.3.4"
expect 1 "latest"
expect 1 "1.2.3-rc.1;rm -rf /"

if (( failures > 0 )); then
  echo "${failures} failure(s)" >&2
  exit 1
fi
echo "validate-release-version: all cases pass"
