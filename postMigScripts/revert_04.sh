#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "revert" "04" "
exec revert_mig_service_account_event_bal_impact_data;
exec revert_mig_service_account_billinfo_data;
"
