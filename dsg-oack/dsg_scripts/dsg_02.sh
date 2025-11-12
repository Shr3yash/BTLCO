#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec post_mig_temp_group_from_family_stg;
exec post_mig_temp_family_purchased_discount;
SQL
)
run_sql "dsg" "02" "$SQL_BLOCK"
