#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "03" "
exec    post_mig_billing_account_payinfo_data;
exec    post_mig_billing_account_billinfo_data;
exec    post_mig_billing_account_bill_data;
exec    post_mig_billing_account_bal_grp_data; -- newly added
exec    post_mig_billing_account_invoice_data;
exec    post_mig_billing_account_status_data;
"
