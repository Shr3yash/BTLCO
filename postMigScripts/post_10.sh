#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "10" "
exec post_mig_serv_acct_update_SA_item_bill;
exec post_mig_serv_acct_update_SA_unbilled_item;
exec post_mig_serv_acct_update_billinfo_bill_obj;
exec post_mig_serv_acct_update_BA_bill;
"
