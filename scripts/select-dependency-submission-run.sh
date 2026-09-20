#!/usr/bin/env bash
# Picks the `Dependency Submission` run to re-run when the review set or a base of a commit
# changed without a new commit (pull request retargeted, reopened, or closed while siblings stay
# open), from the run listing of that head sha (`GET /repos/{owner}/{repo}/actions/workflows/
# dependency-submission.yml/runs?event=pull_request&head_sha=<sha>`) and the open pull request
# listing (`GET /repos/{owner}/{repo}/pulls?state=open`). The latest run may belong to the pull
# request that has just been closed: only runs bound to a pull request still open at that head
# qualify, and the most recent one wins. A same-repository run is bound through its
# `pull_requests`; a fork run has none (GitHub leaves it empty) and is bound through its head
# repository, branch and sha. Prints `<run id> <status>` on stdout (the caller re-runs a
# completed run and lets a running one finish). Exit 0 when a run is found, 1 otherwise, 2 on
# usage error.
# Usage: select-dependency-submission-run.sh <runs.json> <pulls.json> <head-sha>
set -euo pipefail

if [[ $# -ne 3 ]]; then
  echo "Usage: $0 <runs.json> <pulls.json> <head-sha>" >&2
  exit 2
fi

runs=$1 pulls=$2 head_sha=$3

for file in "${runs}" "${pulls}"; do
  if [[ ! -f "${file}" ]]; then
    echo "File not found: ${file}" >&2
    exit 1
  fi
done

python3 - "${runs}" "${pulls}" "${head_sha}" <<'PY'
import json
import sys

runs_file, pulls_file, head_sha = sys.argv[1:]

try:
    with open(runs_file, encoding="utf-8") as handle:
        runs = json.load(handle)["workflow_runs"]
    with open(pulls_file, encoding="utf-8") as handle:
        pulls = [pull for pull in json.load(handle) if pull.get("state") == "open" and (pull.get("head") or {}).get("sha") == head_sha]
except (ValueError, KeyError, AttributeError, TypeError) as error:
    print(f"Malformed listing ({error})", file=sys.stderr)
    sys.exit(1)

open_numbers = {pull["number"] for pull in pulls}
open_heads = {((pull["head"].get("repo") or {}).get("full_name"), pull["head"].get("ref")) for pull in pulls}


def bound_to_open_pull(run):
    if run.get("head_sha") != head_sha:
        return False
    numbers = {pull.get("number") for pull in run.get("pull_requests") or []}
    if numbers:
        return bool(numbers & open_numbers)
    return ((run.get("head_repository") or {}).get("full_name"), run.get("head_branch")) in open_heads


candidates = [run for run in runs if bound_to_open_pull(run)]
if not candidates:
    print(f"No Dependency Submission run bound to an open pull request at {head_sha}", file=sys.stderr)
    sys.exit(1)

selected = max(candidates, key=lambda run: (str(run.get("created_at")), run["id"]))
print(f"Selected run {selected['id']} ({selected.get('status')}) among {len(candidates)}", file=sys.stderr)
print(f"{selected['id']} {selected.get('status')}")
PY
