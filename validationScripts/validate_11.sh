#!/usr/bin/env bash
# Runs validation group 11; expects ./common.sh to exist and define run_sql
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec    validate_post_mig_service_account_insert_bill;
exec    validate_post_mig_serv_acct_update_SA_item_bill;
exec    validate_post_mig_serv_acct_update_SA_unbilled_item;
exec    validate_post_mig_serv_acct_update_billinfo_bill_obj;
exec    validate_post_mig_serv_acct_update_BA_bill;
SQL
)

run_sql "validate" "11" "$SQL_BLOCK"
