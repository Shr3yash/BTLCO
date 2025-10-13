#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "post" "13" "
exec post_mig_bill_pofile_service;
exec post_mig_bill_pofile_service_profile;
exec post_mig_bill_pofile_service_bal_grp;
exec post_mig_bill_pofile_service_pur_prod;
"
