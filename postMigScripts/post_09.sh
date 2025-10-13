#!/usr/bin/env bash
# Step 9: Pre-create /bill POIDs, populate temp/main bill tables, then run post-mig SA billing procs.
# Reads creds from pod_master.config: STG_CREDENTIAL, PIN1_USER, PIN_CREDENTIAL

set -euo pipefail

# --- Config ---
POD_CONF="${POD_CONF:-/path/pod_master.config}"   # to be confurmed with azra
LOG_DIR="${LOG_DIR:-./logs}"
mkdir -p "$LOG_DIR"

# --- Read credentials (same vars showed) ---
credential=$(egrep -v "^#" "$POD_CONF" | grep "STG_CREDENTIAL" | cut -d'=' -f2)
pin1_user=$(egrep -v "^#" "$POD_CONF" | grep "PIN1_USER" | cut -d'=' -f2)
credential_pin1=$(egrep -v "^#" "$POD_CONF" | grep "PIN_CREDENTIAL" | cut -d'=' -f2)

start_ts="$(date '+%Y%m%d_%H%M%S')"
log_file="$LOG_DIR/post-09_${start_ts}.log"
echo " post_09.sh started at $start_ts" | tee -a "$log_file"

# Helper: run a SQL*Plus block and print its stdout (for capturing a value)
sqlval() {
  local cred="$1"
  local block="$2"
  sqlplus -s "$cred" <<SQL | tr -d ' \t\r' | sed '/^$/d'
SET HEADING OFF FEEDBACK OFF VERIFY OFF ECHO OFF PAGESIZE 0 LINESIZE 32767 TRIMSPOOL ON
SET SERVEROUTPUT ON
$block
EXIT
SQL
}

# Helper: run a SQL*Plus block for effects (DDL/DML/PROC), with strict error handling
sqlexec() {
  local cred="$1"
  local label="$2"
  local block="$3"
  {
    echo ""
    echo "----- $label -----"
    sqlplus -s "$cred" <<SQL
WHENEVER OSERROR EXIT 1
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET ECHO ON FEEDBACK ON SERVEROUTPUT ON VERIFY OFF TIMING ON PAGESIZE 500 LINESIZE 32767
SPOOL $LOG_DIR/${label}_${start_ts}.lst
$block
SPOOL OFF
EXIT
SQL
  } >>"$log_file" 2>&1
  echo " $label" | tee -a "$log_file"
}

# 1) Truncate staging table
sqlexec "$credential" "truncate_pin1_stg_long_poids" "
TRUNCATE TABLE pin1.STG_LONG_POIDS;
"

# 2) Compute apply_date_1 from earliest bill_t.created_t (assumed epoch seconds)
apply_date_1="$(sqlval "$credential" "
DECLARE
  v_out VARCHAR2(20);
BEGIN
  SELECT TO_CHAR( (DATE '1970-01-01' + ((MIN(created_t) - 1)/(24*60*60))), 'dd-mm-yyyy')
    INTO v_out
    FROM bill_t;
  DBMS_OUTPUT.PUT_LINE(v_out);
END;
/
")"
echo " apply_date_1 = $apply_date_1" | tee -a "$log_file"

if [[ -z "$apply_date_1" ]]; then
  echo " Could not compute apply_date_1 (bill_t empty?)" | tee -a "$log_file"
  exit 1
fi

# 3) Compute no_of_max_poid_1 = max daily count of bills + 1% headroom
no_of_max_poid_1="$(sqlval "$credential" "
WITH per_day AS (
  SELECT TO_CHAR((DATE '1970-01-01' + (created_t/(24*60*60))),'yyyy-mm-dd') AS dt,
         COUNT(*) AS cnt
  FROM bill_t
  GROUP BY TO_CHAR((DATE '1970-01-01' + (created_t/(24*60*60))),'yyyy-mm-dd')
),
mx AS (
  SELECT MAX(cnt) AS m FROM per_day
)
SELECT TRIM(TO_CHAR(ROUND(m + (m*0.01)))) FROM mx;
")"
echo " no_of_max_poid_1 = $no_of_max_poid_1" | tee -a "$log_file"

if [[ -z "$no_of_max_poid_1" ]]; then
  echo " Could not compute no_of_max_poid_1" | tee -a "$log_file"
  exit 1
fi

# 4) Create /bill POIDs in PIN1
sqlexec "$credential_pin1" "create_bill_poids" "
BEGIN
  pin1.create_poids_m('/bill', '$apply_date_1', 'PIN1', $no_of_max_poid_1);
END;
/
"

# 5) Step-9 flow (moved up): reset SA temp bill table and run only the required post-mig procs
sqlexec "$credential" "truncate_temp_sa_bill_t" "
TRUNCATE TABLE temp_sa_bill_T;
"

sqlexec "$credential" "post_mig_step9_procs" "
EXEC post_mig_serv_acct_insert_temp_bill;
EXEC post_mig_serv_acct_update_temp_bill_poid;
EXEC post_mig_srvc_acct_temp_sa_bill_previous_total;
EXEC post_mig_srvc_acct_temp_sa_bill_total_due;
EXEC post_mig_service_account_insert_bill;
COMMIT;
"

echo " post_09.sh completed. Logs: $log_file"
