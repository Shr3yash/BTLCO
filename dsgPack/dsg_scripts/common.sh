#!/usr/bin/env bash
#
# common.sh
# ----------
# Shared helper functions for executing Oracle SQL*Plus tasks
# with consistent error handling, logging, and spool output.
#
# Expected environment:
#   CONF_FILE  - path to config file (default: /abspnshare/migration/brm/landing_dir/master.config)
#   The config file must define:
#       DB_USER=<username>
#       DB_PASS=<password>
#       DB_TNS=<tns alias>
#   Optional:
#       LOG_DIR=<log output path>  (defaults to ./logs)
#
# Typical usage:
#   source ./common.sh
#   run_sql "post" "09" "exec post_mig_serv_acct_insert_temp_bill;"
#   run_sql_file "revert" "03" "/path/to/script.sql"
#

set -euo pipefail

# -------------------------
# Load configuration
# -------------------------
CONF_FILE="${CONF_FILE:-/abspnshare/migration/brm/landing_dir/master.config}"
if [[ ! -f "$CONF_FILE" ]]; then
  echo "Config not found: $CONF_FILE" >&2
  exit 1
fi

# shellcheck source=/dev/null
source "$CONF_FILE"

: "${DB_USER:?DB_USER not set in $CONF_FILE}"
: "${DB_PASS:?DB_PASS not set in $CONF_FILE}"
: "${DB_TNS:?DB_TNS not set in $CONF_FILE}"

LOG_DIR="${LOG_DIR:-./logs}"
mkdir -p "$LOG_DIR"

timestamp() { date +"%Y%m%d_%H%M%S"; }

# -------------------------
# Internal function
# -------------------------
_sqlplus_exec() {
  local label="$1"
  local ts
  ts=$(timestamp)
  local log_file="$LOG_DIR/${label}_${ts}.log"

  sqlplus -s "${DB_USER}/${DB_PASS}@${DB_TNS}" >"$log_file" 2>&1 <<SQL
WHENEVER OSERROR EXIT 1
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET ECHO ON FEEDBACK ON SERVEROUTPUT ON VERIFY OFF TIMING ON PAGESIZE 500 LINESIZE 32767 TRIMSPOOL ON
SPOOL $LOG_DIR/${label}_spool_${ts}.lst
@$TMP_SQL_FILE
SPOOL OFF
EXIT
SQL

  local rc=$?
  if [[ $rc -ne 0 ]]; then
    echo "${label} FAILED. Check log: $log_file"
    return $rc
  fi
  echo "${label} completed successfully. Log: $log_file"
}

# -------------------------
# Public functions
# -------------------------

# run_sql <suite> <id> <sql_text>
# Executes inline SQL text with strict error handling.
run_sql() {
  local suite="$1"
  local id="$2"
  local sql_text="$3"
  local label="${suite}-${id}"

  TMP_SQL_FILE=$(mktemp -t "${label}.XXXX.sql")
  trap 'rm -f "$TMP_SQL_FILE"' EXIT

  cat >"$TMP_SQL_FILE" <<SQL
WHENEVER OSERROR EXIT 1
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET ECHO ON FEEDBACK ON SERVEROUTPUT ON VERIFY OFF TIMING ON PAGESIZE 500 LINESIZE 32767 TRIMSPOOL ON
${sql_text}
EXIT
SQL

  _sqlplus_exec "$label"
  rm -f "$TMP_SQL_FILE"
  trap - EXIT
}

# run_sql_file <suite> <id> <path/to/sqlfile>
# Executes a standalone SQL file with controlled error handling.
run_sql_file() {
  local suite="$1"
  local id="$2"
  local file="$3"
  local label="${suite}-${id}"

  [[ -f "$file" ]] || { echo "SQL file not found: $file" >&2; return 1; }

  TMP_SQL_FILE=$(mktemp -t "${label}.XXXX.sql")
  trap 'rm -f "$TMP_SQL_FILE"' EXIT

  cat >"$TMP_SQL_FILE" <<SQL
WHENEVER OSERROR EXIT 1
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET ECHO ON FEEDBACK ON SERVEROUTPUT ON VERIFY OFF TIMING ON PAGESIZE 500 LINESIZE 32767 TRIMSPOOL ON
@$file
EXIT
SQL

  _sqlplus_exec "$label"
  rm -f "$TMP_SQL_FILE"
  trap - EXIT
}

# run_plsql <suite> <id> <plsql block>
# Executes an anonymous PL/SQL block (useful for quick checks or test logic).
run_plsql() {
  local suite="$1"
  local id="$2"
  local plsql="$3"
  run_sql "$suite" "$id" "$plsql"
}
