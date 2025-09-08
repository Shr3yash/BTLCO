#!/usr/bin/env bash
# Runs the BEFORE hierarchy recon SQL on dev2 using sqlplus.
# Usage:
#   ./run_recon_before.sh /abspnshare/migration/brm/landing_dir/master.config 01_before_hierarchy.sql
# If args are omitted, it will try to use env defaults from the config.

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

# shellcheck source=/dev/null
source "$CONF_FILE"

: "${PIN1_CREDENTIAL:?PIN1_CREDENTIAL not set in config}"
: "${SQLPLUS_SETTINGS:?SQLPLUS_SETTINGS not set in config}"

echo "==> Running BEFORE recon: $SQL_FILE"
echo "==> Using credential: ${PIN1_USER:-unknown}@${DB_ENV_IP:-unknown}"

sqlplus -s "$PIN1_CREDENTIAL" <<EOF
$SQLPLUS_SETTINGS
WHENEVER SQLERROR EXIT SQL.SQLCODE
@${SQL_FILE}
EXIT
EOF

echo "==> BEFORE recon completed."
