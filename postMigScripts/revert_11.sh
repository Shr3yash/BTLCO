#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "revert" "11" "
exec revert_mig_dept_acct_cons_flag_billinfo_data;
exec revert_mig_department_account_data;
"
