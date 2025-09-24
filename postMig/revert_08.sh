#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "revert" "08" "
exec revert_mig_billing_account_invoice;
exec revert_mig_billing_account_bill;
exec revert_mig_billing_account_payinfo;
exec revert_mig_billing_account_billinfo;
"
