#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
for n in 01 02; do
  "${SCRIPT_DIR}/dsg_revert_${n}.sh"
done
echo "All DSG revert groups completed."
