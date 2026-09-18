#!/usr/bin/env bash
# Prints the last release of the Scala artifacts on Maven Central (the <release> entry of the
# mobilityid_3 maven-metadata.xml), to be passed to sbt as -Dmobilityid.mimaBaseline=<version>.
# Exit 0: a release exists and was printed.
# Exit 1: nothing published yet (HTTP 404); nothing is printed and MiMa has no baseline.
# Exit 2: Central could not be questioned or answered something unexpected. Callers must not read 2
#         as "no baseline": skipping the compatibility check on a transport failure would hide a break.
set -euo pipefail

base_url=${CENTRAL_BASE_URL:-https://repo1.maven.org/maven2/dev/juherr/mobilityid}
url="${base_url}/mobilityid_3/maven-metadata.xml"

body=$(mktemp)
trap 'rm -f "${body}"' EXIT
curl_status=0
http_code=$(curl --connect-timeout 5 --max-time 10 --silent --output "${body}" \
  --write-out '%{http_code}' "${url}") || curl_status=$?
if [[ "${curl_status}" -ne 0 ]]; then
  echo "Maven Central is unreachable (curl exit ${curl_status}): ${url}" >&2
  exit 2
fi
case "${http_code}" in
  200) ;;
  404)
    echo "No Scala release on Maven Central yet: ${url}" >&2
    exit 1
    ;;
  *)
    echo "Unexpected HTTP ${http_code} from Maven Central: ${url}" >&2
    exit 2
    ;;
esac

release=$(sed -n 's/.*<release>\([^<]*\)<\/release>.*/\1/p' "${body}" | head -n 1)
if [[ -z "${release}" ]]; then
  echo "No <release> entry in ${url}" >&2
  exit 2
fi
echo "${release}"
