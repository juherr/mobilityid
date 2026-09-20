#!/usr/bin/env bash
# Proves the retract directives of go.mod repair `@latest` once the retract-only go/v1.1.1 tag
# exists: a file-based module proxy replays the versions proxy.golang.org knows (v0.1.0 and the
# phantom v1.1.0 synthesized from the Scala root tag) plus the released v0.2.0 and the
# retract-only v1.1.1, both carrying this repository's go.mod. The go command reads
# retractions from the highest version (v1.1.1), so `@latest` must fall back to v0.2.0.
set -euo pipefail
cd "$(dirname "$0")/../.."
module="mobilityid.juherr.dev/go"
work="$(mktemp -d "${TMPDIR:-/tmp}/mobilityid-go-retract.XXXXXX")"
trap 'rm -rf "${work}"' EXIT
proxy="${work}/proxy/${module}/@v"
mkdir -p "${proxy}"

add_version() { # version, go.mod file
  printf '{"Version":"%s","Time":"2026-01-01T00:00:00Z"}\n' "$1" > "${proxy}/$1.info"
  cp "$2" "${proxy}/$1.mod"
  echo "$1" >> "${proxy}/list"
}
printf 'module %s\n' "${module}" > "${work}/synthesized.mod"
add_version v0.1.0 "${work}/synthesized.mod"
add_version v1.1.0 "${work}/synthesized.mod"
add_version v0.2.0 go.mod
add_version v1.1.1 go.mod

export GOPROXY="file://${work}/proxy" GOSUMDB=off GOFLAGS=-mod=mod GOTOOLCHAIN=local
export GOMODCACHE="${work}/modcache" GOPATH="${work}/gopath"
cd "${work}"

status=0
check() { # description, expected, actual
  if [[ "$2" == "$3" ]]; then echo "ok   $1"; else echo "FAIL $1: expected '$2', got '$3'"; status=1; fi
}
check "retracted versions are hidden" "${module} v0.2.0" "$(go list -m -versions "${module}")"
check "every version is listed with -retracted" "${module} v0.1.0 v0.2.0 v1.1.0 v1.1.1" "$(go list -m -retracted -versions "${module}")"
check "@latest resolves to the released version" "${module} v0.2.0" "$(go list -m "${module}@latest")"
check "retractions are read from v1.1.1" "${module} v1.1.1 (retracted)" "$(go list -m -retracted "${module}@latest")"
exit "${status}"
