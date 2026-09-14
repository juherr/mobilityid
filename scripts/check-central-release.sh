#!/usr/bin/env bash
# Exit 0: every mobilityid4j payload for <version> resolves from Maven Central.
# Exit 1: Central answered and at least one payload is absent.
# Exit 2: Central could not be questioned. Callers must not read 2 as "absent":
#         deploying on a transport failure would republish an already published version.
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <version>" >&2
  exit 2
fi

version=$1
base_url=${CENTRAL_BASE_URL:-https://repo1.maven.org/maven2/dev/juherr/mobilityid}
base="${base_url}/mobilityid4j/${version}/mobilityid4j-${version}"

resolves() {
  local url=$1
  local http_code curl_status=0
  http_code=$(curl --connect-timeout 5 --max-time 10 --silent --head --output /dev/null \
    --write-out '%{http_code}' "${url}") || curl_status=$?
  if [[ "${curl_status}" -ne 0 ]]; then
    echo "Maven Central is unreachable (curl exit ${curl_status}): ${url}" >&2
    exit 2
  fi
  case "${http_code}" in
    200) return 0 ;;
    404) return 1 ;;
    *)
      echo "Unexpected HTTP ${http_code} from Maven Central: ${url}" >&2
      exit 2
      ;;
  esac
}

for payload in "${base}.pom" "${base}.jar" "${base}-sources.jar" "${base}-javadoc.jar" "${base}.module"; do
  if ! resolves "${payload}"; then
    echo "Not published to Maven Central: ${payload}" >&2
    exit 1
  fi
done
