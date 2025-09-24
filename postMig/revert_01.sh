#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "revert" "01" "
exec revert_mig_service_account_service_data;
exec revert_mig_service_account_profile_data;
exec revert_mig_service_account_bal_data;
exec revert_mig_service_account_subscription_data;
"
