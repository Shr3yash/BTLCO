#!/usr/bin/env bash
# Runs validation group 09; expects ./common.sh to exist and define run_sql
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec    validate_post_mig_service_account_bal_data;
exec    validate_post_mig_srvc_acct_common_srvc_bal_data;
exec    validate_post_mig_service_account_subscription_data;
exec    validate_post_mig_srvc_acct_common_srvc_subscription_data;
SQL
)

run_sql "validate" "09" "$SQL_BLOCK"
