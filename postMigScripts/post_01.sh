#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "01" "
exec post_mig_customer_account_data;
exec post_mig_customer_account_status_data;
"
