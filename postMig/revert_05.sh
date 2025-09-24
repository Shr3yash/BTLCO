#!/usr/bin/env bash
set -euo pipefail
source ./common.sh
run_sql "revert" "05" "
exec revert_mig_bill_profile_service;
exec revert_mig_bill_profile_service_profile;
exec revert_mig_bill_profile_service_bal_grp;
exec revert_mig_bill_profile_service_pur_prod;
"
