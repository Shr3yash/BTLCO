#!/usr/bin/env bash
# Runs validation group 01; expects ./common.sh to exist and define run_sql
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec    validate_post_mig_customer_account_data;
exec    validate_post_mig_customer_account_status_data;
SQL
)

run_sql "validate" "01" "$SQL_BLOCK"
