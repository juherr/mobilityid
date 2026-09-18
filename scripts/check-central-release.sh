#!/usr/bin/env bash
# Usage: check-central-release.sh <version> [--no-module] [artifactId...]
#   artifactId defaults to mobilityid4j (Gradle: pom, jar, sources, javadoc and .module metadata).
#   --no-module drops the Gradle module metadata payload (sbt artifacts do not publish one).
# Exit 0: every payload of every artifact for <version> resolves from Maven Central.
# Exit 1: Central answered and no payload exists: the version was never published.
# Exit 3: Central answered and only some payloads exist: propagation in progress, or a previous
#         upload. Callers must wait, never publish again.
# Exit 2: Central could not be questioned, or missing version. Callers must not read 2 as "absent":
#         deploying on a transport failure would republish an already published version.
# Every payload is queried (no short-circuit) so that 1 really means "nothing exists".
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <version> [--no-module] [artifactId...]" >&2
  exit 2
fi

version=$1
shift
payloads=(.pom .jar -sources.jar -javadoc.jar .module)
if [[ "${1:-}" == --no-module ]]; then
  payloads=(.pom .jar -sources.jar -javadoc.jar)
  shift
fi
artifacts=("$@")
if [[ ${#artifacts[@]} -eq 0 ]]; then
  artifacts=(mobilityid4j)
fi
base_url=${CENTRAL_BASE_URL:-https://repo1.maven.org/maven2/dev/juherr/mobilityid}

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

present=0
absent=0
for artifact in "${artifacts[@]}"; do
  base="${base_url}/${artifact}/${version}/${artifact}-${version}"
  for payload in "${payloads[@]}"; do
    if resolves "${base}${payload}"; then
      present=$((present + 1))
    else
      echo "Not on Maven Central: ${base}${payload}" >&2
      absent=$((absent + 1))
    fi
  done
done
if (( absent == 0 )); then
  exit 0
fi
if (( present == 0 )); then
  echo "Version ${version} is not published to Maven Central." >&2
  exit 1
fi
echo "Version ${version} is partially visible on Maven Central (${present} payloads present, ${absent} absent): propagation in progress or an earlier upload; never publish it again." >&2
exit 3
