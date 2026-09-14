#!/usr/bin/env bash
# Asserts that an npm tarball of @juherr/mobilityid is publishable: right name and version,
# built JS entry and type declarations present, no sources or tests leaked. Exit 0 when valid,
# 1 otherwise. Usage: verify-npm-package.sh <tarball.tgz> <version>
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
forbid() {
  if grep -Eq "^package/$1" <<<"${entries}"; then
    echo "Must not be in the package: $1" >&2
    exit 1
  fi
}

require package.json
require dist/index.js
require dist/index.d.ts
require README.md
forbid 'src/'
forbid 'test/'
forbid 'node_modules/'

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
