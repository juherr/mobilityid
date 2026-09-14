#!/usr/bin/env bash
# Checks the release guard without publishing anything:
#   1. verifyRelease refuses a SNAPSHOT version;
#   2. verifyRelease refuses a release version without signing inputs;
#   3. the guard is scheduled before the nmcp upload task, not only before the lifecycle task.
set -euo pipefail

cd "$(dirname "$0")/.."

expect_failure() {
  local message="$1"; shift
  local output
  if output=$("$@" 2>&1); then
    echo "expected failure: $message" >&2
    echo "${output}" >&2
    exit 1
  fi
  if ! grep -Fq "${message}" <<<"${output}"; then
    echo "expected message '${message}' not found in:" >&2
    echo "${output}" >&2
    exit 1
  fi
  echo "ok: ${message}"
}

unset SIGNING_KEY SIGNING_PASSWORD

expect_failure "Release publishing requires a non-SNAPSHOT version" \
  ./gradlew --quiet verifyRelease
expect_failure "Release publishing requires signingKey/SIGNING_KEY and signingPassword/SIGNING_PASSWORD" \
  ./gradlew --quiet verifyRelease -PreleaseVersion=9.9.9

graph=$(MAVEN_CENTRAL_USERNAME=dry MAVEN_CENTRAL_PASSWORD=run \
  ./gradlew --dry-run -PreleaseVersion=9.9.9 publishAggregationToCentralPortal 2>&1 | grep -E '^:[A-Za-z]+ SKIPPED$')
guard_line=$(grep -n '^:verifyRelease SKIPPED$' <<<"${graph}" | cut -d: -f1)
upload_line=$(grep -n '^:nmcpPublishAggregationToCentralPortal SKIPPED$' <<<"${graph}" | cut -d: -f1)
test -n "${guard_line}" || { echo "verifyRelease is not in the publish task graph" >&2; exit 1; }
test -n "${upload_line}" || { echo "nmcpPublishAggregationToCentralPortal is not in the publish task graph" >&2; exit 1; }
if (( guard_line >= upload_line )); then
  echo "verifyRelease (${guard_line}) must run before nmcpPublishAggregationToCentralPortal (${upload_line})" >&2
  exit 1
fi
echo "ok: verifyRelease runs before the Central Portal upload"
