#!/usr/bin/env bash
# Accepts a release version only if every registry will: strict SemVer 2.0.0 core with an
# optional prerelease (node-semver rules, as applied by npm), no leading "v", no build metadata
# (Maven and git tags would carry it, npm strips it), no SNAPSHOT. Exit 0 when valid, 1 otherwise.
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <version>" >&2
  exit 2
fi

version=$1
numeric='(0|[1-9][0-9]*)'
identifier='(0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)'
semver="^${numeric}\\.${numeric}\\.${numeric}(-${identifier}(\\.${identifier})*)?$"

if [[ ! "${version}" =~ ${semver} ]]; then
  echo "Invalid release version '${version}': expected X.Y.Z or X.Y.Z-prerelease (SemVer 2.0.0, no build metadata)" >&2
  exit 1
fi
if [[ "${version}" == *[Ss][Nn][Aa][Pp][Ss][Hh][Oo][Tt]* ]]; then
  echo "Invalid release version '${version}': snapshots are never released" >&2
  exit 1
fi
echo "${version}"
