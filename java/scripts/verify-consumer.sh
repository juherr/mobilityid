#!/usr/bin/env bash
# Publishes mobilityid4j to an isolated repository and consumes it from a separate Gradle build
# (consumer-smoke) on the module path, the way a downstream application would.
set -euo pipefail

cd "$(dirname "$0")/.."

version="${1:-0.0.0-smoke}"
repository="$(pwd)/build/smoke-repo"

rm -rf "${repository}"
./gradlew --quiet -PreleaseVersion="${version}" publishMavenJavaPublicationToSmokeRepository
test -f "${repository}/dev/juherr/mobilityid/mobilityid4j/${version}/mobilityid4j-${version}.pom"
test -f "${repository}/dev/juherr/mobilityid/mobilityid4j/${version}/mobilityid4j-${version}.module"

./gradlew --quiet -p consumer-smoke \
  -PmobilityidVersion="${version}" \
  -PmobilityidRepository="file://${repository}" \
  smoke
