#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "03" "
exec post_mig_billing_account_payinfo_data;
exec post_mig_billing_account_bilinfo_data;
exec post_mig_billing_account_bill_data;
exec post_mig_billing_account_invoice_data;
exec post_mig_billing_account_status_data;
"
