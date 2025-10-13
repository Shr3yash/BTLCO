#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "08" "
exec post_mig_service_account_bal_data;
exec post_mig_srvc_acct_common_srvc_bal_data;
exec post_mig_service_account_subscription_data;
exec post_mig_srvc_acct_common_srvc_subscription_data;
"
