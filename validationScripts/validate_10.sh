#!/usr/bin/env bash
# Runs validation group 10; expects ./common.sh to exist and define run_sql
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec    validate_post_mig_serv_acct_insert_temp_bill;
exec    validate_post_mig_serv_acct_update_temp_bill_poid;
exec    validate_post_mig_srvc_acct_temp_sa_bill_previous_total;
exec    validate_post_mig_srvc_acct_temp_sa_bill_total_due;
SQL
)

run_sql "validate" "10" "$SQL_BLOCK"
