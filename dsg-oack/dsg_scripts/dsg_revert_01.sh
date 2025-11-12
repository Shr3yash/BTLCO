#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec revert_mig_dsg_group;
exec revert_mig_dsg_purchased_discount_t;
SQL
)
run_sql "dsg_revert" "01" "$SQL_BLOCK"
