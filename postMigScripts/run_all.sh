#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-}"
[[ "$MODE" == "post" || "$MODE" == "revert" ]] || {
  echo "Usage: $0 post|revert" >&2
  exit 1
}

START="${START:-1}"
END="${END:-14}"

pad2() { printf "%02d" "$1"; }

for ((i=START; i<=END; i++)); do
  id="$(pad2 "$i")"
  script="${MODE}_${id}.sh"
  if [[ ! -x "$script" ]]; then
    echo "Missing or not executable: $script" >&2
    exit 1
  fi
  echo "Running $script ..."
  "./$script"
  echo "Done $script"
done

echo "Completed ${MODE} suite from ${START} to ${END}."
