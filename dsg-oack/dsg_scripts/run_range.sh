#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
  cat <<USAGE
Usage:
  $0 dsg <start> <end>
  $0 dsg_revert <start> <end>

Examples:
  $0 dsg 03 07          # runs dsg_03.sh through dsg_07.sh
  $0 dsg_revert 01 02   # runs dsg_revert_01.sh and _02.sh

Notes:
  - Expects common.sh in the same folder as these scripts.
  - start/end must be two-digit numbers (e.g., 01, 02, 03...).
USAGE
}

[[ $# -eq 3 ]] || { usage; exit 1; }

suite="$1"
start="$2"
end="$3"

case "$suite" in
  dsg)        max="08"; prefix="dsg_";;
  dsg_revert) max="02"; prefix="dsg_revert_";;
  *) echo "Unknown suite: $suite"; usage; exit 1;;
esac

if ! [[ "$start" =~ ^[0-9]{2}$ && "$end" =~ ^[0-9]{2}$ ]]; then
  echo "start/end must be two-digit numbers (e.g., 01)"; exit 1
fi
if [[ "$start" > "$end" ]]; then
  echo "start must be <= end"; exit 1
fi
if [[ "$end" > "$max" ]]; then
  echo "end exceeds max group ($max) for suite $suite"; exit 1
fi

cur="$start"
while [[ "$cur" -le "$end" ]]; do
  script="${SCRIPT_DIR}/${prefix}${cur}.sh"
  if [[ -x "$script" ]]; then
    echo ">> Running ${suite} group ${cur}"
    "$script"
  else:
    echo "!! Script not found or not executable: $script"
    exit 1
  fi
  cur=$(printf "%02d" $((10#$cur + 1)))
done

echo "Completed $suite groups ${start}..${end}"
    