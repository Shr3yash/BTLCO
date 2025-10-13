#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "06" "
exec post_mig_service_account_item_data;
exec post_mig_srvc_acct_common_srvc_item_data;
"
