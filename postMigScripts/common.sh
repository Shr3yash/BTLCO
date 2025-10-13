#!/usr/bin/env bash
#  thes are just some helpers for running SQL*Plus tasks safely with logs

#   source ./common.sh
#   run_sql "post" "01" "exec foo; exec bar;"                # inline SQL
#   run_sql_file "revert" "03" "/path/to/file.sql"           # from .sql file

set -euo pipefail

CONF_FILE="${CONF_FILE:-/abspnshare/migration/brm/landing_dir/master.config}"
if [[ ! -f "$CONF_FILE" ]]; then
  echo "Config not found: $CONF_FILE" >&2
  exit 1
fi
# shellcheck source=/dev/null
source "$CONF_FILE"

# Validate required env vars
: "${DB_USER:?DB_USER not set in config}"
: "${DB_PASS:?DB_PASS not set in config}"
: "${DB_TNS:?DB_TNS not set in config}"

LOG_DIR="${LOG_DIR:-./logs}"
mkdir -p "$LOG_DIR"

timestamp() { date +"%Y%m%d_%H%M%S"; }

# Internal: executes SQL passed on stdin
_sqlplus_exec() {
  local label="$1"  # e.g., post-01 or revert-07
  local log_file="$LOG_DIR/${label}_$(timestamp).log"

  # -s for silent (we echo what we need via SET)
  sqlplus -s "${DB_USER}/${DB_PASS}@${DB_TNS}" >"$log_file" 2>&1 <<'SQL'
WHENEVER OSERROR EXIT 1
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET ECHO ON FEEDBACK ON SERVEROUTPUT ON VERIFY OFF TIMING ON PAGESIZE 500 LINESIZE 32767
-- Spool controlled externally via shell redirection
SQL
  # Now send the real SQL (from caller) with a second sqlplus to preserve WHENEVER
  sqlplus -s "${DB_USER}/${DB_PASS}@${DB_TNS}" >>"$log_file" 2>&1
  local rc=$?
  if [[ $rc -ne 0 ]]; then
    echo "${label} FAILED. See log: $log_file"
    return $rc
  fi
  echo "${label} OK. Log: $log_file"
}

run_sql() {
  # Args: suite (post|revert) id(01..14) sql_text
  local suite="$1"
  local id="$2"
  local sql_text="$3"
  local label="${suite}-${id}"

  # Feed a proper SQL*Plus script with error handling
  _sqlplus_exec "$label" <<SQL
WHENEVER OSERROR EXIT 1
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET ECHO ON FEEDBACK ON SERVEROUTPUT ON VERIFY OFF TIMING ON
SPOOL $LOG_DIR/${label}_spool_$(timestamp).lst
${sql_text}
SPOOL OFF
EXIT
SQL
}

run_sql_file() {
  # Args: suite (post|revert) id(01..14) file.sql
  local suite="$1"
  local id="$2"
  local file="$3"
  local label="${suite}-${id}"
  [[ -f "$file" ]] || { echo "SQL file not found: $file" >&2; return 1; }

  _sqlplus_exec "$label" <<SQL
WHENEVER OSERROR EXIT 1
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET ECHO ON FEEDBACK ON SERVEROUTPUT ON VERIFY OFF TIMING ON
SPOOL $LOG_DIR/${label}_spool_$(timestamp).lst
@$file
SPOOL OFF
EXIT
SQL
}
