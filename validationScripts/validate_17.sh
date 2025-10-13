#!/usr/bin/env bash
# Runs validation group 17; expects ./common.sh to exist and define run_sql
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec    validate_post_mig_bill_pofile_service_item;
exec    validate_post_mig_bill_pofile_service_event;
exec    validate_post_mig_bill_pofile_service_event_bal_imp;
SQL
)

run_sql "validate" "17" "$SQL_BLOCK"
