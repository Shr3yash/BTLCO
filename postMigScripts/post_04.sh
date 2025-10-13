#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "04" "
exec post_mig_service_account_status_data;
exec post_mig_ar_item_data;
exec post_mig_ar_item_event_data;
exec post_mig_ar_item_event_bal_impact_data;
"
