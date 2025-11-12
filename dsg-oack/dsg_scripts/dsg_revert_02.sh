#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh}"

SQL_BLOCK=$(cat <<'SQL'
exec revert_mig_dsg_group_sharing_discounts_t;
exec revert_mig_dsg_group_sharing_members_t;
exec revert_mig_dsg_ordered_balgroup_t;
SQL
)
run_sql "dsg_revert" "02" "$SQL_BLOCK"
