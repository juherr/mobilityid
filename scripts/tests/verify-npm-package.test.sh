#!/usr/bin/env bash
# Exercises scripts/verify-npm-package.sh with synthetic tarballs: a complete package passes,
# a package missing its JS entry, its type declarations, with the wrong version, leaking
# sources, shipping a LICENSE that is not the full Apache 2.0 text or a NOTICE without the
# copyright line fails.
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
  # Skeleton of the full Apache 2.0 text: the markers the check looks for, in order.
  cat > "${dir}/LICENSE" <<'TXT'
                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

   1. Definitions.

   END OF TERMS AND CONDITIONS
TXT
  echo "Copyright (c) 2026 Julien Herr, and respective contributors" > "${dir}/NOTICE"
  for op in "$@"; do
    case "${op}" in
      no-js) rm "${dir}/dist/index.js" ;;
      no-dts) rm "${dir}/dist/index.d.ts" ;;
      leak-src) mkdir -p "${dir}/src" && echo "x" > "${dir}/src/index.ts" ;;
      no-license) rm "${dir}/LICENSE" ;;
      # The short header with a URL is not a copy of the license.
      license-short) printf 'Licensed under the Apache License, Version 2.0 (the "License");\nhttp://www.apache.org/licenses/LICENSE-2.0\n' > "${dir}/LICENSE" ;;
      license-truncated) sed -i.bak '/END OF TERMS/d' "${dir}/LICENSE" && rm "${dir}/LICENSE.bak" ;;
      no-notice) rm "${dir}/NOTICE" ;;
      notice-no-copyright) echo "no copyright line" > "${dir}/NOTICE" ;;
      stray-root) echo "secret=1" > "${dir}/.env" ;;
      stray-dir) mkdir -p "${dir}/scripts" && echo "x" > "${dir}/scripts/release.sh" ;;
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
expect 1 "missing LICENSE" "$(make_tarball nolic 1.2.3 no-license)" 1.2.3
expect 1 "LICENSE is the short header, not the license text" "$(make_tarball short 1.2.3 license-short)" 1.2.3
expect 1 "LICENSE truncated before the end of terms" "$(make_tarball trunc 1.2.3 license-truncated)" 1.2.3
expect 1 "missing NOTICE" "$(make_tarball nonotice 1.2.3 no-notice)" 1.2.3
expect 1 "NOTICE without the copyright line" "$(make_tarball nocopyright 1.2.3 notice-no-copyright)" 1.2.3
expect 1 "unexpected root file (.env)" "$(make_tarball stray 1.2.3 stray-root)" 1.2.3
expect 1 "unexpected directory (scripts/)" "$(make_tarball straydir 1.2.3 stray-dir)" 1.2.3
expect 1 "tarball does not exist" "${work}/missing.tgz" 1.2.3

if (( failures > 0 )); then
  echo "${failures} failure(s)" >&2
  exit 1
fi
echo "verify-npm-package: all cases pass"
