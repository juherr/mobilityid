#!/usr/bin/env bash
# Validates the provenance of a GitHub dependency-graph snapshot uploaded as an artifact by an
# untrusted `pull_request` run (fork or Dependabot) before a privileged `workflow_run` job submits
# it. The artifact is attacker-controlled: without these checks a fork could upload a snapshot
# carrying `ref: refs/heads/main` and the current `main` sha, and mask Dependabot alerts.
# The snapshot is accepted only when it is bound to the triggering run and its pull request:
#   - `sha` is the head commit of the run (`workflow_run.head_sha`);
#   - `ref` is `refs/pull/<N>/merge`, the ref gradle/actions records for a pull_request event;
#   - `job.id` is the triggering run id and `job.correlator` the expected job correlator;
#   - pull request <N> is in the given list (`GET /repos/{owner}/{repo}/pulls?head=<owner>:<branch>`),
#     open, with that head sha and coming from the expected head repository;
#   - every manifest `source_location` is a canonical repository-relative path under `java/`
#     (no `..`, `.` or `//` segment, not absolute): the fork may only describe the Java graph of
#     its own pull request, not shadow the manifests GitHub parses itself (npm, Composer, Go,
#     Actions) through a path such as `java/../ts/package.json`.
# The content itself (resolved transitive dependencies) is the job of verify-dependency-graph.sh.
# Exit 0 when accepted, 1 when rejected, 2 on usage error.
# Usage: validate-dependency-graph-provenance.sh <snapshot.json> <pulls.json> <head-sha> <run-id> <correlator> <head-repo>
set -euo pipefail

if [[ $# -ne 6 ]]; then
  echo "Usage: $0 <snapshot.json> <pulls.json> <head-sha> <run-id> <correlator> <head-repo>" >&2
  exit 2
fi

for file in "$1" "$2"; do
  if [[ ! -f "${file}" ]]; then
    echo "File not found: ${file}" >&2
    exit 1
  fi
done

python3 - "$@" <<'PY'
import json
import posixpath
import re
import sys

snapshot_file, pulls_file, head_sha, run_id, correlator, head_repo = sys.argv[1:]

failures = []


def check(condition, label, detail):
    if condition:
        print(f"ok: {label}", file=sys.stderr)
    else:
        failures.append(f"{label} ({detail})")


try:
    with open(snapshot_file, encoding="utf-8") as handle:
        snapshot = json.load(handle)
    with open(pulls_file, encoding="utf-8") as handle:
        pulls = json.load(handle)

    check(snapshot.get("sha") == head_sha, "snapshot sha is the run head sha", f"got {snapshot.get('sha')!r}")

    ref = snapshot.get("ref") or ""
    match = re.fullmatch(r"refs/pull/(\d+)/merge", ref)
    check(match is not None, "snapshot ref is a pull request merge ref", f"got {ref!r}")

    job = snapshot.get("job") or {}
    check(str(job.get("id")) == run_id, "snapshot job id is the triggering run", f"got {job.get('id')!r}")
    check(job.get("correlator") == correlator, "snapshot job correlator matches", f"got {job.get('correlator')!r}")

    sources = [(manifest.get("file") or {}).get("source_location") for manifest in (snapshot.get("manifests") or {}).values()]
    foreign = [
        source
        for source in sources
        if not (isinstance(source, str) and source.startswith("java/") and posixpath.normpath(source) == source)
    ]
    check(not foreign, "every manifest is a canonical path under java/", f"got {foreign!r}")

    if match is not None:
        number = int(match.group(1))
        pull = next((p for p in pulls if p.get("number") == number), None)
        check(pull is not None, f"pull request #{number} is a pull request of the head branch", "not in the list")
        if pull is not None:
            head = pull.get("head") or {}
            repo = (head.get("repo") or {}).get("full_name")
            check(pull.get("state") == "open", f"pull request #{number} is open", f"state {pull.get('state')!r}")
            check(head.get("sha") == head_sha, f"pull request #{number} head is the run head sha", f"got {head.get('sha')!r}")
            check(repo == head_repo, f"pull request #{number} comes from the run head repository", f"got {repo!r}")
except (ValueError, AttributeError, TypeError) as error:
    failures.append(f"malformed input ({error})")

if failures:
    for failure in failures:
        print(f"REJECTED: {failure}", file=sys.stderr)
    sys.exit(1)
PY
