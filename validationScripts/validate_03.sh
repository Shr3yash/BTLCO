#!/usr/bin/env bash
# Runs validation group 03; expects ./common.sh to exist and define run_sql
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec    validate_post_mig_billing_account_payinfo_data;
exec    validate_post_mig_billing_account_bilinfo_data;
exec    validate_post_mig_billing_account_bill_data;
exec    validate_post_mig_billing_account_invoice_data;
exec    validate_post_mig_billing_account_status_data;
SQL
)

run_sql "validate" "03" "$SQL_BLOCK"
