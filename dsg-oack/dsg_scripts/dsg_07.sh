#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec post_mig_insert_group;
exec post_mig_insert_group_sharing_discounts;
exec post_mig_insert_group_sharing_members;
SQL
)
run_sql "dsg" "07" "$SQL_BLOCK"
