#!/usr/bin/env bash
# Picks the dependency-graph artifact to download from a workflow run's artifact listing
# (`GET /repos/{owner}/{repo}/actions/runs/{run_id}/artifacts?name=<name>`). A re-run keeps the
# artifacts of the previous attempts on the same run id, so the name alone is ambiguous: among
# the non-expired artifacts of that name whose workflow run is bound to the expected head sha,
# the one created last (the most recent attempt that uploaded a graph) is selected and its id
# printed on stdout. Exit 0 when one is found, 1 otherwise, 2 on usage error.
# Usage: select-dependency-graph-artifact.sh <artifacts.json> <name> <head-sha>
set -euo pipefail

if [[ $# -ne 3 ]]; then
  echo "Usage: $0 <artifacts.json> <name> <head-sha>" >&2
  exit 2
fi

listing=$1 name=$2 head_sha=$3

if [[ ! -f "${listing}" ]]; then
  echo "File not found: ${listing}" >&2
  exit 1
fi

python3 - "${listing}" "${name}" "${head_sha}" <<'PY'
import json
import sys

listing, name, head_sha = sys.argv[1:]

try:
    with open(listing, encoding="utf-8") as handle:
        artifacts = json.load(handle)["artifacts"]
    candidates = [
        artifact
        for artifact in artifacts
        if artifact.get("name") == name
        and not artifact.get("expired")
        and (artifact.get("workflow_run") or {}).get("head_sha") == head_sha
    ]
except (ValueError, KeyError, AttributeError, TypeError) as error:
    print(f"Malformed artifact listing ({error})", file=sys.stderr)
    sys.exit(1)

if not candidates:
    print(f"No non-expired artifact named {name} bound to {head_sha} in the listing", file=sys.stderr)
    sys.exit(1)

selected = max(candidates, key=lambda artifact: (str(artifact.get("created_at")), artifact["id"]))
print(f"Selected artifact {selected['id']} created at {selected.get('created_at')} among {len(candidates)}", file=sys.stderr)
print(selected["id"])
PY
