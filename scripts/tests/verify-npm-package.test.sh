#!/usr/bin/env bash
# Exercises scripts/verify-npm-package.sh with synthetic tarballs: a complete package passes,
# a package missing its JS entry, its type declarations, with the wrong version or leaking
# sources fails.
set -euo pipefail

cd "$(dirname "$0")/../.."
script=scripts/verify-npm-package.sh
failures=0
work=$(mktemp -d)
trap 'rm -rf "${work}"' EXIT

make_tarball() {
  local name=$1 version=$2; shift 2
  local dir="${work}/${name}/package"
  rm -rf "${work}/${name}"
  mkdir -p "${dir}/dist"
  cat > "${dir}/package.json" <<JSON
{"name":"@juherr/mobilityid","version":"${version}","type":"module","main":"./dist/index.js","types":"./dist/index.d.ts","exports":{".":{"types":"./dist/index.d.ts","import":"./dist/index.js"}}}
JSON
  echo "export {};" > "${dir}/dist/index.js"
  echo "export {};" > "${dir}/dist/index.d.ts"
  echo "# readme" > "${dir}/README.md"
  for op in "$@"; do
    case "${op}" in
      no-js) rm "${dir}/dist/index.js" ;;
      no-dts) rm "${dir}/dist/index.d.ts" ;;
      leak-src) mkdir -p "${dir}/src" && echo "x" > "${dir}/src/index.ts" ;;
    esac
  done
  tar -czf "${work}/${name}.tgz" -C "${work}/${name}" package
  echo "${work}/${name}.tgz"
}

expect() {
  local expected=$1 label=$2 tarball=$3 version=$4 status=0
  "${script}" "${tarball}" "${version}" >/dev/null 2>&1 || status=$?
  if [[ "${status}" -ne "${expected}" ]]; then
    echo "FAIL: ${label} -> exit ${status}, expected ${expected}"
    failures=$((failures + 1))
  else
    echo "ok: ${label} -> ${status}"
  fi
}

expect 0 "complete package" "$(make_tarball ok 1.2.3)" 1.2.3
expect 1 "version mismatch" "$(make_tarball ok 1.2.3)" 1.2.4
expect 1 "missing dist/index.js" "$(make_tarball nojs 1.2.3 no-js)" 1.2.3
expect 1 "missing dist/index.d.ts" "$(make_tarball nodts 1.2.3 no-dts)" 1.2.3
expect 1 "sources leaked into the package" "$(make_tarball leak 1.2.3 leak-src)" 1.2.3
expect 1 "tarball does not exist" "${work}/missing.tgz" 1.2.3

if (( failures > 0 )); then
  echo "${failures} failure(s)" >&2
  exit 1
fi
echo "verify-npm-package: all cases pass"
