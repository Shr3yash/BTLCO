#!/usr/bin/env bash
set -euo pipefail

CONF_FILE="${1:-/abspnshare/migration/brm/landing_dir/master.config}"
SQL_FILE="${2:-01_before_hierarchy.sql}"

if [[ ! -f "$CONF_FILE" ]]; then
  echo "Config not found: $CONF_FILE" >&2
  exit 1
fi
if [[ ! -f "$SQL_FILE" ]]; then
  echo "SQL file not found: $SQL_FILE" >&2
  exit 1
fi

source "$CONF_FILE"

: "${PIN1_CREDENTIAL:?PIN1_CREDENTIAL not set in config}"
: "${SQLPLUS_SETTINGS:?SQLPLUS_SETTINGS not set in config}"

echo "==> Running BEFORE recon: $SQL_FILE"
sqlplus -s "$PIN1_CREDENTIAL" <<EOF
$SQLPLUS_SETTINGS
SET TERMOUT ON HEADING ON FEEDBACK ON PAGESIZE 5000 LINESIZE 200 TRIMSPOOL ON
WHENEVER SQLERROR EXIT SQL.SQLCODE
BEGIN
   FOR r IN (SELECT table_name FROM user_tables WHERE table_name LIKE 'RECON\_%\_BEFORE' ESCAPE '\') LOOP
      EXECUTE IMMEDIATE 'DROP TABLE '||r.table_name;
   END LOOP;
END;
/
@$SQL_FILE
EXIT
EOF
echo "==> BEFORE recon completed."
