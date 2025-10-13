#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "revert" "14" "
exec revert_mig_cust_acct_cons_flag_billinfo_data;
exec revert_mig_customer_account_data;
"
