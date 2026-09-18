#!/usr/bin/env bash
# Exercises scripts/check-central-release.sh against a local HTTP stub:
#   every payload 200 -> 0; a 404 on any payload -> 1; 5xx, unexpected code or transport failure -> 2.
set -euo pipefail

cd "$(dirname "$0")/../.."
script=scripts/check-central-release.sh
failures=0
port=$(python3 -c 'import socket; s=socket.socket(); s.bind(("127.0.0.1",0)); print(s.getsockname()[1]); s.close()')
stub_log=$(mktemp)

# The scenario is the first path segment (/<mode>/dev/juherr/...), so one stub serves every case.
python3 - "${port}" >"${stub_log}" 2>&1 <<'PY' &
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer

class Stub(BaseHTTPRequestHandler):
    def do_HEAD(self):
        mode = self.path.split("/")[1]
        if mode == "ok":
            code = 200
        elif mode == "missing-javadoc":
            code = 404 if self.path.endswith("-javadoc.jar") else 200
        elif mode == "server-error":
            code = 500 if self.path.endswith(".jar") else 200
        elif mode == "unexpected":
            code = 302
        elif mode == "no-gradle-module":
            code = 404 if self.path.endswith(".module") else 200
        elif mode == "missing-scala-3":
            code = 404 if "/mobilityid_3/" in self.path else 200
        else:
            code = 200
        self.send_response(code)
        self.end_headers()
    def log_message(self, *args):
        pass

HTTPServer(("127.0.0.1", int(sys.argv[1])), Stub).serve_forever()
PY
stub_pid=$!
trap 'kill "${stub_pid}" 2>/dev/null || true; wait "${stub_pid}" 2>/dev/null || true; rm -f "${stub_log}"' EXIT
for _ in $(seq 1 50); do
  curl --silent --head "http://127.0.0.1:${port}/" >/dev/null 2>&1 && break
  sleep 0.1
done

expect() {
  local expected=$1 mode=$2 base=$3 status=0
  shift 3
  CENTRAL_BASE_URL="${base}" "${script}" 1.2.3 "$@" >/dev/null 2>&1 || status=$?
  if [[ "${status}" -ne "${expected}" ]]; then
    echo "FAIL: mode=${mode} base=${base} -> exit ${status}, expected ${expected}"
    failures=$((failures + 1))
  else
    echo "ok: mode=${mode} -> ${status}"
  fi
}

base="http://127.0.0.1:${port}"
expect 0 ok "${base}/ok/dev/juherr/mobilityid"
expect 1 missing-javadoc "${base}/missing-javadoc/dev/juherr/mobilityid"
expect 2 server-error "${base}/server-error/dev/juherr/mobilityid"
expect 2 unexpected "${base}/unexpected/dev/juherr/mobilityid"
expect 2 refused "http://127.0.0.1:1/dev/juherr/mobilityid"   # connection refused: transport failure

# Scala artifacts: several artifact ids, no Gradle module metadata.
scala_artifacts=(mobilityid_2.13 mobilityid_3 mobilityid-interpolators_2.13 mobilityid-interpolators_3)
expect 0 no-gradle-module "${base}/no-gradle-module/dev/juherr/mobilityid" --no-module "${scala_artifacts[@]}"
expect 1 no-gradle-module "${base}/no-gradle-module/dev/juherr/mobilityid" "${scala_artifacts[@]}"   # default expects .module
expect 1 missing-scala-3 "${base}/missing-scala-3/dev/juherr/mobilityid" --no-module "${scala_artifacts[@]}"
expect 0 missing-scala-3 "${base}/missing-scala-3/dev/juherr/mobilityid" --no-module mobilityid_2.13   # only the listed ids count

if (( failures > 0 )); then
  echo "${failures} failure(s)" >&2
  exit 1
fi
echo "check-central-release: all cases pass"
