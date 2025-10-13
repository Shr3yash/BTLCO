#!/usr/bin/env bash
# Runs validation group 06; expects ./common.sh to exist and define run_sql
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec    validate_post_mig_service_account_billinfo_data;
exec    validate_post_mig_service_account_bill_data;
SQL
)

run_sql "validate" "06" "$SQL_BLOCK"
