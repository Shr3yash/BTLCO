#!/usr/bin/env bash
# Runs validation group 14; expects ./common.sh to exist and define run_sql
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec    validate_post_mig_dept_acct_cons_flag_service_data;
exec    validate_post_mig_dept_acct_cons_flag_bal_grp_data;
exec    validate_post_mig_dept_acct_cons_flag_payinfo_data;
SQL
)

run_sql "validate" "14" "$SQL_BLOCK"
