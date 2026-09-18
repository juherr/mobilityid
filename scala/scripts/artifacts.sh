#!/usr/bin/env bash
# Sourced by the verification scripts: the artifacts a Scala release publishes and the check
# that every payload of every artifact exists under a Maven layout.
scala_artifacts=(mobilityid_2.13 mobilityid_3 mobilityid-interpolators_2.13 mobilityid-interpolators_3)
scala_payloads=(.pom .jar -sources.jar -javadoc.jar)

# require_published_payloads <repository> <version> [check]
#   <repository> is a Maven layout root; [check] is run with the payload path as its argument
#   after the existence check (defaults to none).
require_published_payloads() {
  local repository=$1 version=$2 check=${3:-}
  local artifact payload path
  for artifact in "${scala_artifacts[@]}"; do
    for payload in "${scala_payloads[@]}"; do
      path="${repository}/dev/juherr/mobilityid/${artifact}/${version}/${artifact}-${version}${payload}"
      test -f "${path}" || { echo "missing published payload: ${path}" >&2; return 1; }
      if [[ -n "${check}" ]]; then
        "${check}" "${path}" || return 1
      fi
    done
  done
}
