#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "14" "
exec post_mig_bill_pofile_service_item;
exec post_mig_bill_pofile_service_event;
exec post_mig_bill_pofile_service_event_bal_imp;
"
