#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec post_mig_insert_ordered_balgroup;
exec post_mig_insert_purchased_discount;
SQL
)
run_sql "dsg" "08" "$SQL_BLOCK"
