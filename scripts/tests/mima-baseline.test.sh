#!/usr/bin/env bash
# Exercises scripts/mima-baseline.sh against a local HTTP stub:
#   metadata with a <release> -> prints it, exit 0; 404 -> nothing, exit 1;
#   5xx, unexpected code, metadata without <release> or transport failure -> 2.
set -euo pipefail

cd "$(dirname "$0")/../.."
script=scripts/mima-baseline.sh
failures=0
port=$(python3 -c 'import socket; s=socket.socket(); s.bind(("127.0.0.1",0)); print(s.getsockname()[1]); s.close()')
stub_log=$(mktemp)

# The scenario is the first path segment (/<mode>/dev/juherr/...), so one stub serves every case.
python3 - "${port}" >"${stub_log}" 2>&1 <<'PY' &
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer

METADATA = b"""<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>dev.juherr.mobilityid</groupId>
  <artifactId>mobilityid_3</artifactId>
  <versioning>
    <latest>1.3.0-RC1</latest>
    <release>1.2.3</release>
    <versions><version>1.2.3</version><version>1.3.0-RC1</version></versions>
  </versioning>
</metadata>
"""

class Stub(BaseHTTPRequestHandler):
    def do_GET(self):
        mode = self.path.split("/")[1]
        body = b""
        if mode == "ok":
            code, body = 200, METADATA
        elif mode == "not-published":
            code = 404
        elif mode == "server-error":
            code = 500
        elif mode == "unexpected":
            code = 302
        elif mode == "no-release":
            code, body = 200, b"<metadata><versioning><latest>1.0.0-SNAPSHOT</latest></versioning></metadata>"
        else:
            code = 200
        self.send_response(code)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)
    def log_message(self, *args):
        pass

HTTPServer(("127.0.0.1", int(sys.argv[1])), Stub).serve_forever()
PY
stub_pid=$!
trap 'kill "${stub_pid}" 2>/dev/null || true; wait "${stub_pid}" 2>/dev/null || true; rm -f "${stub_log}"' EXIT
for _ in $(seq 1 50); do
  curl --silent "http://127.0.0.1:${port}/" >/dev/null 2>&1 && break
  sleep 0.1
done

expect() {
  local expected_status=$1 expected_output=$2 mode=$3 base=$4 status=0 output
  output=$(CENTRAL_BASE_URL="${base}" "${script}" 2>/dev/null) || status=$?
  if [[ "${status}" -ne "${expected_status}" || "${output}" != "${expected_output}" ]]; then
    echo "FAIL: mode=${mode} -> exit ${status} output '${output}', expected ${expected_status} '${expected_output}'"
    failures=$((failures + 1))
  else
    echo "ok: mode=${mode} -> ${status} '${output}'"
  fi
}

base="http://127.0.0.1:${port}"
expect 0 1.2.3 ok "${base}/ok/dev/juherr/mobilityid"
expect 1 "" not-published "${base}/not-published/dev/juherr/mobilityid"
expect 2 "" server-error "${base}/server-error/dev/juherr/mobilityid"
expect 2 "" unexpected "${base}/unexpected/dev/juherr/mobilityid"
expect 2 "" no-release "${base}/no-release/dev/juherr/mobilityid"
expect 2 "" refused "http://127.0.0.1:1/dev/juherr/mobilityid"   # connection refused: transport failure

if (( failures > 0 )); then
  echo "${failures} failure(s)" >&2
  exit 1
fi
echo "mima-baseline: all cases pass"
