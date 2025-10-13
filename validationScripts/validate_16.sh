#!/usr/bin/env bash
# Runs validation group 16; expects ./common.sh to exist and define run_sql
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

SQL_BLOCK=$(cat <<'SQL'
exec    validate_post_mig_bill_pofile_service;
exec    validate_post_mig_bill_pofile_service_profile;
exec    validate_post_mig_bill_pofile_service_bal_grp;
exec    validate_post_mig_bill_pofile_service_pur_prod;
SQL
)

run_sql "validate" "16" "$SQL_BLOCK"
