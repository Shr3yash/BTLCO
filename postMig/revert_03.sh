#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "revert" "03" "
exec revert_mig_service_account_item_data;
exec revert_mig_service_account_event_data;
"
