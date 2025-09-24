#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "revert" "02" "
exec revert_post_mig_serv_acct_update_SA_item_bill;
exec revert_post_mig_serv_acct_update_BA_bill;
"
