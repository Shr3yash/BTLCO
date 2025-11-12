#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec post_mig_update_temp_group_poid;
exec post_mig_insert_temp_mandatory_discounts;
exec post_mig_temp_family_mandatory_discounts;
SQL
)
run_sql "dsg" "03" "$SQL_BLOCK"
