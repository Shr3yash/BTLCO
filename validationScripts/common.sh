#!/usr/bin/env bash
# common.sh — shared helpers for running SQL*Plus tasks with consistent logging
# Expected alongside validate_*.sh and run_all.sh
# Reads DB credentials from master.config unless overridden by env.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# -----------------------------------------------------------------------------
# Config & environment
# -----------------------------------------------------------------------------
# Override with: export CONF_FILE=/path/to/master.config
CONF_FILE="${CONF_FILE:-/abspnshare/migration/brm/landing_dir/master.config}"
if [[ ! -f "$CONF_FILE" ]]; then
  echo "Config not found: $CONF_FILE" >&2
  exit 1
fi

# shellcheck source=/dev/null
source "$CONF_FILE"

# Required vars coming from master.config (or your env override)
: "${DB_USER:?DB_USER not set in config}"
: "${DB_PASS:?DB_PASS not set in config}"
: "${DB_TNS:?DB_TNS not set in config}"

# Logs live next to scripts by default; override with: export LOG_DIR=/some/path
LOG_DIR="${LOG_DIR:-${SCRIPT_DIR}/logs}"
mkdir -p "$LOG_DIR"

timestamp() { date +"%Y%m%d_%H%M%S"; }

# -----------------------------------------------------------------------------
# Public API
# -----------------------------------------------------------------------------
# run_sql "validate" "07" "$SQL_BLOCK"
run_sql() {
  local suite="$1"   # e.g., validate | post | revert
  local id="$2"      # e.g., 01..99
  local sql_text="$3"
  local label="${suite}-${id}"
  local log_file="${LOG_DIR}/${label}_$(timestamp).log"
  local spool_file="${LOG_DIR}/${label}_spool_$(timestamp).lst"

  # Single sqlplus session with strict error handling
  sqlplus -s "${DB_USER}/${DB_PASS}@${DB_TNS}" >"$log_file" 2>&1 <<SQL
WHENEVER OSERROR EXIT 1
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET ECHO ON FEEDBACK ON SERVEROUTPUT ON VERIFY OFF TIMING ON PAGESIZE 500 LINESIZE 32767
SPOOL ${spool_file}
${sql_text}
SPOOL OFF
EXIT
SQL

  local rc=$?
  if [[ $rc -ne 0 ]]; then
    echo "${label} FAILED. See log: ${log_file}"
    return $rc
  fi
  echo "${label} OK. Log: ${log_file}"
}

# run_sql_file "validate" "03" "/path/to/file.sql"
run_sql_file() {
  local suite="$1"
  local id="$2"
  local file="$3"
  [[ -f "$file" ]] || { echo "SQL file not found: $file" >&2; return 1; }

  local sql
  sql=$(<"$file")
  run_sql "$suite" "$id" "$sql"
}
