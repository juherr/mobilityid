#!/usr/bin/env bash
# gpg front-end for git tag --sign in CI: non-interactive, passphrase from a protected file.
set -euo pipefail

: "${GPG_PASSPHRASE_FILE:?GPG_PASSPHRASE_FILE must point to a protected passphrase file}"

exec gpg --batch --pinentry-mode loopback --passphrase-file "${GPG_PASSPHRASE_FILE}" "$@"
