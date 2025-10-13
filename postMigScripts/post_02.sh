#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "02" "
exec post_mig_department_account_data;
exec post_mig_department_account_status_data;
"
