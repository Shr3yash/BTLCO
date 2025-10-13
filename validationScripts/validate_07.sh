#!/usr/bin/env bash
# Runs validation group 07; expects ./common.sh to exist and define run_sql
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec    validate_post_mig_service_account_item_data;
exec    validate_post_mig_srvc_acct_common_srvc_item_data;
SQL
)

run_sql "validate" "07" "$SQL_BLOCK"
