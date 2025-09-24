#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "revert" "06" "
exec revert_mig_bill_profile_service_item;
exec revert_mig_bill_profile_service_event;
exec revert_mig_bill_profile_service_event_bal_imp;
"
