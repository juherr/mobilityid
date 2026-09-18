#!/usr/bin/env bash
# Exercises scripts/report-trusted-review-status.sh against a fake `gh` on PATH that records its
# arguments: the commit status is posted on the given sha with the shared context, the given
# state and target URL; a bad state or missing argument is a usage error and calls nothing.
set -euo pipefail

cd "$(dirname "$0")/../.."
script=scripts/report-trusted-review-status.sh
failures=0
work=$(mktemp -d)
trap 'rm -rf "${work}"' EXIT

mkdir "${work}/bin"
cat > "${work}/bin/gh" <<'FAKE'
#!/usr/bin/env bash
printf '%s\n' "$@" > "${GH_RECORD}"
FAKE
chmod +x "${work}/bin/gh"
export PATH="${work}/bin:${PATH}" GH_RECORD="${work}/gh-args" GITHUB_REPOSITORY=owner/repo

sha=1111111111111111111111111111111111111111
url=https://github.com/owner/repo/actions/runs/1

# expect <status> <label> <args...>
expect() {
  local expected=$1 label=$2 status=0; shift 2
  rm -f "${GH_RECORD}"
  "${script}" "$@" >/dev/null 2>&1 || status=$?
  if [[ "${status}" -ne "${expected}" ]]; then
    echo "FAIL: ${label} -> exit ${status}, expected ${expected}"
    failures=$((failures + 1))
  else
    echo "ok: ${label} -> ${status}"
  fi
}

# recorded <label> <expected line...>: every expected line must be an argument of the gh call
recorded() {
  local label=$1; shift
  for line in "$@"; do
    if ! grep -qxF -- "${line}" "${GH_RECORD}" 2>/dev/null; then
      echo "FAIL: ${label}: gh was not called with '${line}' (got: $(tr '\n' ' ' < "${GH_RECORD}" 2>/dev/null))"
      failures=$((failures + 1))
      return
    fi
  done
  echo "ok: ${label}"
}

expect 0 "success status" "${sha}" success "${url}"
recorded "success status posted on the sha with the shared context" \
  "repos/owner/repo/statuses/${sha}" "state=success" "context=Trusted dependency review" "target_url=${url}"
expect 0 "failure status" "${sha}" failure "${url}"
recorded "failure state forwarded" "state=failure"
expect 0 "pending status" "${sha}" pending "${url}"
recorded "pending state forwarded" "state=pending"

expect 2 "unknown state" "${sha}" green "${url}"
[[ ! -f "${GH_RECORD}" ]] && echo "ok: unknown state calls nothing" || { echo "FAIL: unknown state called gh"; failures=$((failures + 1)); }
expect 2 "missing arguments" "${sha}" success

if (( failures > 0 )); then
  echo "${failures} report-trusted-review-status check(s) failed"
  exit 1
fi
echo "all report-trusted-review-status checks passed"
