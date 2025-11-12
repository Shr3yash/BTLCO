#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec post_mig_update_temp_purchased_discount_poid;
exec post_mig_update_temp_ordered_balgroup_poid;
SQL
)
run_sql "dsg" "06" "$SQL_BLOCK"
