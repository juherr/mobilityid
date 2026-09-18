#!/usr/bin/env bash
# Checks the release guard without publishing anything:
#   1. verifyRelease refuses a SNAPSHOT version;
#   2. verifyRelease refuses a release version without the Central Portal / PGP inputs;
#   3. verifyRelease refuses a release version when the GPG keyring has no secret key;
#   4. publishSigned runs the guard first: nothing is staged when the guard fails;
#   5. with every input present, publishSigned signs (gpg, throw-away key) and stages both
#      modules for both Scala versions in target/sona-staging, where sonaRelease picks them up;
#   6. MiMa really analyzes a baseline when -Dmobilityid.mimaBaseline is given.
# Nothing is uploaded: only sonaRelease talks to the Central Portal, and it is never run here.
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

unset RELEASE_VERSION SONATYPE_USERNAME SONATYPE_PASSWORD PGP_PASSPHRASE SMOKE_REPOSITORY

expect_failure "Release publishing requires a non-SNAPSHOT version" \
  sbt --server --batch verifyRelease
expect_failure "Release publishing requires SONATYPE_USERNAME, SONATYPE_PASSWORD, PGP_PASSPHRASE" \
  env RELEASE_VERSION=9.9.9 sbt --server --batch verifyRelease

empty_keyring=$(mktemp -d)
trap 'rm -rf "${empty_keyring}"' EXIT
expect_failure "Release publishing requires a GPG secret key in the keyring" \
  env RELEASE_VERSION=9.9.9 SONATYPE_USERNAME=dry SONATYPE_PASSWORD=run PGP_PASSPHRASE=dry GNUPGHOME="${empty_keyring}" \
  sbt --server --batch verifyRelease

staging="$(pwd)/target/sona-staging"
rm -rf "${staging}"
expect_failure "Release publishing requires SONATYPE_USERNAME, SONATYPE_PASSWORD, PGP_PASSPHRASE" \
  env RELEASE_VERSION=9.9.9 sbt --server --batch publishSigned
if [[ -d "${staging}" ]]; then
  echo "publishSigned staged artifacts although verifyRelease failed" >&2
  exit 1
fi
echo "ok: publishSigned runs verifyRelease before staging anything"

# Green proof of the signing/staging path, with a throw-away key in an isolated keyring.
gpg --batch --quiet --homedir "${empty_keyring}" --pinentry-mode loopback --passphrase throw-away 2>/dev/null \
  --quick-generate-key "mobilityid smoke <smoke@example.invalid>" ed25519 sign never
env RELEASE_VERSION=0.0.0-signed SONATYPE_USERNAME=dry SONATYPE_PASSWORD=run PGP_PASSPHRASE=throw-away \
  GNUPGHOME="${empty_keyring}" sbt --server --batch "+publishSigned" >/dev/null
for artifact in mobilityid_2.13 mobilityid_3 mobilityid-interpolators_2.13 mobilityid-interpolators_3; do
  base="${staging}/dev/juherr/mobilityid/${artifact}/0.0.0-signed/${artifact}-0.0.0-signed"
  for payload in .pom .jar -sources.jar -javadoc.jar; do
    test -f "${base}${payload}" || { echo "not staged: ${base}${payload}" >&2; exit 1; }
    test -f "${base}${payload}.asc" || { echo "not signed: ${base}${payload}.asc" >&2; exit 1; }
    gpg --batch --quiet --homedir "${empty_keyring}" --verify "${base}${payload}.asc" "${base}${payload}" 2>/dev/null \
      || { echo "invalid signature: ${base}${payload}.asc" >&2; exit 1; }
  done
done
rm -rf "${staging}"
echo "ok: publishSigned signs and stages both modules for both Scala versions in target/sona-staging"

# MiMa wiring: publish a baseline to an isolated repository and compare the build against it.
baseline_repository="$(pwd)/target/mima-baseline-repo"
rm -rf "${baseline_repository}"
mkdir -p "${baseline_repository}"
SMOKE_REPOSITORY="${baseline_repository}" RELEASE_VERSION=0.0.0-mima sbt --server --batch "+publish" >/dev/null
# `show mimaFindBinaryIssues` prints the analyzed baseline even when sbt replays a cached result;
# the "Found N incompatibilities" log line does not survive a cache hit.
output=$(SMOKE_REPOSITORY="${baseline_repository}" sbt --server --batch -Dmobilityid.mimaBaseline=0.0.0-mima \
  "+show core/mimaFindBinaryIssues; +show interpolators/mimaFindBinaryIssues" 2>&1)
for artifact in mobilityid_2.13 mobilityid-interpolators_2.13 mobilityid_3 mobilityid-interpolators_3; do
  if ! grep -Fq "dev.juherr.mobilityid:${artifact}:0.0.0-mima -> (List(),List())" <<<"${output}"; then
    echo "MiMa did not analyze ${artifact} against the 0.0.0-mima baseline:" >&2
    echo "${output}" >&2
    exit 1
  fi
done
echo "ok: MiMa analyzes the baseline given by -Dmobilityid.mimaBaseline on both Scala versions"
