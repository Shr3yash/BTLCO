#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "revert" "09" "
exec revert_mig_dept_acct_cons_flag_invoice_data;
exec revert_mig_dept_acct_cons_flag_bill_data;
exec revert_mig_dept_acct_cons_flag_payinfo_data;
"
