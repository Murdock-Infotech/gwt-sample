#!/usr/bin/env bash
set -euo pipefail

# Auto-recompile watcher for GWT Super Dev Mode (macOS + Linux).
# Requires:
# - macOS: fswatch (brew install fswatch), curl
# - Linux: inotifywait (inotify-tools), curl
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

OS_NAME="$(uname -s)"

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

echo "Watching for changes (${OS_NAME}):"
echo " - ${CLIENT_SRC}"
echo " - ${SHARED_SRC}"
echo "Debounce: ${DEBOUNCE_SECONDS}s"
echo "Recompile URL: ${URL}"
echo ""

# Notes:
# - macOS: `-o` emits one event per batch; together with `-l` it serves as debounce.
# - Linux: we do a small manual debounce by sleeping then draining queued events.
# - `--exclude` is a regex applied to full paths.
EXCLUDE_REGEX='(/\.git/|/target/|/war/|/lib/)'

trigger_recompile() {
  ts="$(date '+%Y-%m-%d %H:%M:%S')"
  printf '[%s] change detected -> recompile... ' "${ts}"
  if curl -fsS "${URL}" >/dev/null; then
    echo "ok"
  else
    echo "failed (CodeServer running on :9876?)"
  fi
}

if [[ "${OS_NAME}" == "Darwin" ]]; then
  if ! command -v fswatch >/dev/null 2>&1; then
    echo "autoRecompile.sh: missing dependency: fswatch"
    echo "Install: brew install fswatch"
    exit 2
  fi

  fswatch -o -r -l "${DEBOUNCE_SECONDS}" --exclude "${EXCLUDE_REGEX}" "${CLIENT_SRC}" "${SHARED_SRC}" \
    | while read -r _; do
        trigger_recompile
      done
elif [[ "${OS_NAME}" == "Linux" ]]; then
  if ! command -v inotifywait >/dev/null 2>&1; then
    echo "autoRecompile.sh: missing dependency: inotifywait"
    echo "Install (Debian/Ubuntu): sudo apt-get install inotify-tools"
    echo "Install (Fedora): sudo dnf install inotify-tools"
    echo "Install (Arch): sudo pacman -S inotify-tools"
    exit 2
  fi

  inotifywait -m -r -e modify,create,delete,move --exclude "${EXCLUDE_REGEX}" "${CLIENT_SRC}" "${SHARED_SRC}" \
    | while read -r _; do
        sleep "${DEBOUNCE_SECONDS}"
        while read -r -t 0.1 _; do :; done
        trigger_recompile
      done
else
  case "${OS_NAME}" in
    MINGW*|MSYS*|CYGWIN*)
      echo "autoRecompile.sh: detected Windows shell: ${OS_NAME}"
      PS_SCRIPT="${SCRIPT_DIR}/autoRecompile.ps1"
      if [[ ! -f "${PS_SCRIPT}" ]]; then
        echo "autoRecompile.sh: not found: ${PS_SCRIPT}"
        exit 2
      fi

      DEBOUNCE_MS="$(awk -v s="${DEBOUNCE_SECONDS}" 'BEGIN { printf "%d", (s * 1000) }')"

      if command -v pwsh >/dev/null 2>&1; then
        exec pwsh "${PS_SCRIPT}" -Url "${URL}" -DebounceMs "${DEBOUNCE_MS}"
      elif command -v powershell >/dev/null 2>&1; then
        exec powershell -ExecutionPolicy Bypass -File "${PS_SCRIPT}" -Url "${URL}" -DebounceMs "${DEBOUNCE_MS}"
      else
        echo "autoRecompile.sh: missing dependency: pwsh or powershell"
        echo "Install PowerShell and retry, or run autoRecompile.ps1 directly."
        exit 2
      fi
      ;;
    *)
      echo "autoRecompile.sh: unsupported OS: ${OS_NAME}"
      exit 2
      ;;
  esac
fi
