#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
for n in $(seq -w 01 08); do
  "${SCRIPT_DIR}/dsg_${n}.sh"
done
echo "All DSG groups completed."
