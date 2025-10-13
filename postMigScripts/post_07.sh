#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "07" "
exec post_mig_service_account_event_data;
exec post_mig_srvc_acct_common_srvc_event_data;
exec post_mig_service_account_event_bal_impact_data;
exec post_mig_srvc_acct_common_srvc_event_bal_impact_data;
"
