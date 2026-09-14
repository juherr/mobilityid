#!/usr/bin/env bash
# Asserts that an npm tarball of @juherr/mobilityid is publishable: right name and version,
# built JS entry and type declarations present, LICENSE with the Apache grant and both copyright
# notices, and nothing outside the allowlist (package.json, README.md, LICENSE, dist/**). Exit 0 when valid, 1 otherwise.
# Usage: verify-npm-package.sh <tarball.tgz> <version>
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <tarball.tgz> <version>" >&2
  exit 2
fi

tarball=$1
version=$2

if [[ ! -f "${tarball}" ]]; then
  echo "Tarball not found: ${tarball}" >&2
  exit 1
fi

entries=$(tar -tzf "${tarball}")

require() {
  if ! grep -Fxq "package/$1" <<<"${entries}"; then
    echo "Missing from the package: $1" >&2
    exit 1
  fi
}

require package.json
require dist/index.js
require dist/index.d.ts
require README.md
require LICENSE

# The packaged LICENSE must carry the Apache 2.0 grant and both copyright lines: the port is a
# derivative of the New Motion Scala code (AGENTS.md, "Provenance"), so its notice is required.
license=$(tar -xzOf "${tarball}" package/LICENSE)
for notice in \
  "Copyright (c) 2014 The New Motion team, and respective contributors" \
  "Copyright (c) 2026 Julien Herr, and respective contributors" \
  "Apache License, Version 2.0"; do
  if ! grep -Fq "${notice}" <<<"${license}"; then
    echo "LICENSE in the package lacks the notice: ${notice}" >&2
    exit 1
  fi
done

# Allowlist: anything else in the tarball is a packaging mistake.
# Directory entries (trailing slash) are allowed for the package root and dist/ only.
unexpected=$(grep -Ev '^package/(package\.json|README\.md|LICENSE|dist/.+|dist/)?$' <<<"${entries}" || true)
if [[ -n "${unexpected}" ]]; then
  echo "Unexpected entries in the package:" >&2
  echo "${unexpected}" >&2
  exit 1
fi

manifest=$(tar -xzOf "${tarball}" package/package.json)
name=$(python3 -c 'import json,sys; print(json.load(sys.stdin)["name"])' <<<"${manifest}")
actual_version=$(python3 -c 'import json,sys; print(json.load(sys.stdin)["version"])' <<<"${manifest}")
if [[ "${name}" != "@juherr/mobilityid" ]]; then
  echo "Unexpected package name: ${name}" >&2
  exit 1
fi
if [[ "${actual_version}" != "${version}" ]]; then
  echo "Package version ${actual_version} does not match release version ${version}" >&2
  exit 1
fi
echo "npm package ${name}@${actual_version}: OK"
