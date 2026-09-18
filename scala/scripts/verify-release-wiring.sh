#!/usr/bin/env bash
# Checks the release guard without publishing anything to a registry:
#   1. verifyRelease refuses a SNAPSHOT version;
#   2. verifyRelease refuses a release version when the GPG keyring has no secret key;
#   3. publishSigned runs the guard first: nothing is staged without the Central Portal / PGP inputs;
#   4. with every input present, publishSigned signs (gpg, throw-away key) and stages both modules
#      for both Scala versions in target/sona-staging, where sonaRelease would pick them up;
#   5. MiMa really analyzes the baseline given by MOBILITYID_MIMA_BASELINE.
# The signed, staged artifacts are moved to target/smoke-repo (version 0.0.0-smoke) so that
# verify-consumer.sh consumes them without a second cross build, and so that a later sonaRelease
# from this checkout cannot upload them. Only sonaRelease talks to the Central Portal; it never runs here.
set -euo pipefail

cd "$(dirname "$0")/.."
# shellcheck source=scala/scripts/artifacts.sh
source scripts/artifacts.sh

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

unset RELEASE_VERSION SONATYPE_USERNAME SONATYPE_PASSWORD PGP_PASSPHRASE SMOKE_REPOSITORY MOBILITYID_MIMA_BASELINE
keyring=$(mktemp -d)
trap 'rm -rf "${keyring}"' EXIT
staging="$(pwd)/target/sona-staging"
smoke_repository="$(pwd)/target/smoke-repo"

expect_failure "Release publishing requires a non-SNAPSHOT version" \
  sbt --server --batch verifyRelease
expect_failure "Release publishing requires a GPG secret key in the keyring" \
  env RELEASE_VERSION=9.9.9 SONATYPE_USERNAME=dry SONATYPE_PASSWORD=run PGP_PASSPHRASE=dry GNUPGHOME="${keyring}" \
  sbt --server --batch verifyRelease

rm -rf "${staging}"
expect_failure "Release publishing requires SONATYPE_USERNAME, SONATYPE_PASSWORD, PGP_PASSPHRASE" \
  env RELEASE_VERSION=9.9.9 sbt --server --batch publishSigned
if [[ -d "${staging}" ]]; then
  echo "publishSigned staged artifacts although verifyRelease failed" >&2
  exit 1
fi
echo "ok: publishSigned runs verifyRelease before staging anything"

gpg --batch --quiet --homedir "${keyring}" --pinentry-mode loopback --passphrase throw-away 2>/dev/null \
  --quick-generate-key "mobilityid smoke <smoke@example.invalid>" ed25519 sign never
env RELEASE_VERSION="${smoke_version}" SONATYPE_USERNAME=dry SONATYPE_PASSWORD=run PGP_PASSPHRASE=throw-away \
  GNUPGHOME="${keyring}" sbt --server --batch "+publishSigned" >/dev/null
require_valid_signature() {
  local payload=$1
  test -f "${payload}.asc" || { echo "not signed: ${payload}.asc" >&2; return 1; }
  gpg --batch --quiet --homedir "${keyring}" --verify "${payload}.asc" "${payload}" 2>/dev/null \
    || { echo "invalid signature: ${payload}.asc" >&2; return 1; }
}
require_published_payloads "${staging}" "${smoke_version}" require_valid_signature
rm -rf "${smoke_repository}"
mv "${staging}" "${smoke_repository}"
echo "ok: publishSigned signs and stages both modules for both Scala versions in target/sona-staging"

# `show mimaFindBinaryIssues` prints the analyzed baseline even when sbt replays a cached result;
# the "Found N incompatibilities" log line does not survive a cache hit.
output=$(SMOKE_REPOSITORY="${smoke_repository}" MOBILITYID_MIMA_BASELINE="${smoke_version}" sbt --server --batch \
  "+show core/mimaFindBinaryIssues interpolators/mimaFindBinaryIssues" 2>&1)
for artifact in "${scala_artifacts[@]}"; do
  if ! grep -Fq "dev.juherr.mobilityid:${artifact}:${smoke_version} -> (List(),List())" <<<"${output}"; then
    echo "MiMa did not analyze ${artifact} against the ${smoke_version} baseline:" >&2
    echo "${output}" >&2
    exit 1
  fi
done
echo "ok: MiMa analyzes the baseline given by MOBILITYID_MIMA_BASELINE on both Scala versions"
