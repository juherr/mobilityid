#!/usr/bin/env bash
# Exercises scripts/wait-central-release.sh against a local HTTP stub:
#   resolvable -> 0 on the first attempt; absent or partially visible after every attempt -> 1;
#   unreachable -> 1 (kept polling, never mistaken for "published"); no version -> 2.
set -euo pipefail

cd "$(dirname "$0")/../.."
script=scripts/wait-central-release.sh
failures=0
port=$(python3 -c 'import socket; s=socket.socket(); s.bind(("127.0.0.1",0)); print(s.getsockname()[1]); s.close()')
stub_log=$(mktemp)

python3 - "${port}" >"${stub_log}" 2>&1 <<'PY' &
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer

class Stub(BaseHTTPRequestHandler):
    def do_HEAD(self):
        mode = self.path.split("/")[1]
        if mode == "ok":
            code = 200
        elif mode == "partial":
            code = 404 if self.path.endswith("-javadoc.jar") else 200
        else:
            code = 404
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
  CENTRAL_BASE_URL="${base}" CENTRAL_WAIT_ATTEMPTS=2 CENTRAL_WAIT_SECONDS=0 "${script}" "$@" >/dev/null 2>&1 || status=$?
  if [[ "${status}" -ne "${expected}" ]]; then
    echo "FAIL: mode=${mode} -> exit ${status}, expected ${expected}"
    failures=$((failures + 1))
  else
    echo "ok: mode=${mode} -> ${status}"
  fi
}

base="http://127.0.0.1:${port}"
expect 0 ok "${base}/ok/dev/juherr/mobilityid" 1.2.3
expect 0 ok-scala "${base}/ok/dev/juherr/mobilityid" 1.2.3 --no-module mobilityid_2.13 mobilityid_3
expect 1 absent "${base}/absent/dev/juherr/mobilityid" 1.2.3
expect 1 partial "${base}/partial/dev/juherr/mobilityid" 1.2.3
expect 1 unreachable "http://127.0.0.1:1/dev/juherr/mobilityid" 1.2.3
expect 2 usage "${base}/ok/dev/juherr/mobilityid"

if (( failures > 0 )); then
  echo "${failures} failure(s)" >&2
  exit 1
fi
echo "wait-central-release: all cases pass"
