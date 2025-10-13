#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "05" "
exec post_mig_service_account_service_data;
exec post_mig_service_account_service_common_data;
exec post_mig_service_account_profile_data;
exec post_mig_srvc_acct_common_srvc_profile_data;
exec post_mig_service_account_billinfo_data;
exec post_mig_service_account_bill_data;
"
