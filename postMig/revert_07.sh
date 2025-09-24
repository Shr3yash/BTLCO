#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "revert" "07" "
exec revert_mig_ar_item_data;
exec revert_mig_ar_item_event_data;
exec revert_mig_ar_item_event_bal_impact_data;
"
