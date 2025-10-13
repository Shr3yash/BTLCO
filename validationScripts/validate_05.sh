#!/usr/bin/env bash
# Runs validation group 05; expects ./common.sh to exist and define run_sql
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec    validate_post_mig_service_account_service_data;
exec    validate_post_mig_service_account_service_common_data;
exec    validate_post_mig_service_account_profile_data;
exec    validate_post_mig_srvc_acct_common_srvc_profile_data;
SQL
)

run_sql "validate" "05" "$SQL_BLOCK"
