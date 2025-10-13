#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "11" "
exec post_mig_cust_acct_cons_flag_service_data;
exec post_mig_cust_acct_cons_flag_bal_grp_data;
exec post_mig_cust_acct_cons_flag_payinfo_data;
exec post_mig_cust_acct_cons_flag_invoice_data;
exec post_mig_cust_acct_cons_flag_billinfo_data;
exec post_mig_cust_acct_cons_flag_bill_data;
"
