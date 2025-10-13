#!/usr/bin/env bash
# Runs all validation groups sequentially; requires ./common.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

for n in $(seq -w 01 17); do
  "${SCRIPT_DIR}/validate_${n}.sh"
done

echo "All validation groups completed."
