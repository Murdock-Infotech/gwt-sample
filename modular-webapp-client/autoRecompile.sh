#!/usr/bin/env bash
set -euo pipefail

# macOS-only auto-recompile watcher for GWT Super Dev Mode.
# Requires: fswatch (brew install fswatch), curl
#
# Watches:
# - modular-webapp-client/src/main/java
# - modular-webapp-shared/src/main/java
#
# Triggers:
#   GET http://localhost:9876/recompile/modularwebapp

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"

CLIENT_SRC="${REPO_ROOT}/modular-webapp-client/src/main/java"
SHARED_SRC="${REPO_ROOT}/modular-webapp-shared/src/main/java"

URL="${1:-http://localhost:9876/recompile/modularwebapp}"
DEBOUNCE_SECONDS="${DEBOUNCE_SECONDS:-0.75}"

if [[ "$(uname -s)" != "Darwin" ]]; then
  echo "autoRecompile.sh: bash auto-recompile is macOS-only (Darwin)."
  echo "autoRecompile.sh: Linux support not implemented yet."
  exit 2
fi

if ! command -v fswatch >/dev/null 2>&1; then
  echo "autoRecompile.sh: missing dependency: fswatch"
  echo "Install: brew install fswatch"
  exit 2
fi

if ! command -v curl >/dev/null 2>&1; then
  echo "autoRecompile.sh: missing dependency: curl"
  exit 2
fi

if [[ ! -d "${CLIENT_SRC}" ]]; then
  echo "autoRecompile.sh: not found: ${CLIENT_SRC}"
  exit 2
fi

if [[ ! -d "${SHARED_SRC}" ]]; then
  echo "autoRecompile.sh: not found: ${SHARED_SRC}"
  exit 2
fi

echo "Watching for changes (macOS):"
echo " - ${CLIENT_SRC}"
echo " - ${SHARED_SRC}"
echo "Debounce: ${DEBOUNCE_SECONDS}s"
echo "Recompile URL: ${URL}"
echo ""

# Notes:
# - `-o` emits one event per batch; together with `-l` it serves as debounce/coalescing.
# - `--exclude` is a regex applied to full paths.
EXCLUDE_REGEX='(/\.git/|/target/|/war/|/lib/)'

fswatch -o -r -l "${DEBOUNCE_SECONDS}" --exclude "${EXCLUDE_REGEX}" "${CLIENT_SRC}" "${SHARED_SRC}" \
  | while read -r _; do
      ts="$(date '+%Y-%m-%d %H:%M:%S')"
      printf '[%s] change detected -> recompile... ' "${ts}"
      if curl -fsS "${URL}" >/dev/null; then
        echo "ok"
      else
        echo "failed (CodeServer running on :9876?)"
      fi
    done
