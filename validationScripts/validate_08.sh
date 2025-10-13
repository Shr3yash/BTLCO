#!/usr/bin/env bash
# Runs validation group 08; expects ./common.sh to exist and define run_sql
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec    validate_post_mig_service_account_event_data;
exec    validate_post_mig_srvc_acct_common_srvc_event_data;
exec    validate_post_mig_service_account_event_bal_impact_data;
exec    validate_post_mig_srvc_acct_common_srvc_event_bal_impact_data;
SQL
)

run_sql "validate" "08" "$SQL_BLOCK"
