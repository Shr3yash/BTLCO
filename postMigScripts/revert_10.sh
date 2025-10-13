#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "revert" "10" "
exec revert_mig_dept_acct_cons_flag_bal_grp_data;
exec revert_mig_dept_acct_cons_flag_service_data;
"
