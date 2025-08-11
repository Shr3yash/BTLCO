#!/usr/bin/env bash
###############################################################################
# run_batelco_mig.sh
#
# Kick‑off wrapper for batelco‑migration‑1.0‑SNAPSHOT‑jar‑with‑dependencies.jar
#
# ARG‑1 (required): MIG_SCRIPTS root directory
# ARG‑2 (optional): Stage number   [default = 1001]
#
# Example:
#   ./run_batelco_mig.sh /abspnshare/migration/brm/landing_dir/MIG_SCRIPTS 1002
###############################################################################

set -euo pipefail          # safer: exit on error, unset vars→error, pipefail

MIG_SCRIPTS=${1:?❌ 1st arg must be MIG_SCRIPTS path}
STAGE=${2:-1001}

# --- Fresh run‑specific properties file -----------------------------------
cp  "$MIG_SCRIPTS/node1/conf/oracle-tele-migration.properties_INITIAL" \
    "$MIG_SCRIPTS/node1/conf/oracle-tele-migration.properties"

sed -i "s+@@STAGE@@+${STAGE}+g" \
    "$MIG_SCRIPTS/node1/conf/oracle-tele-migration.properties"

cd  "$MIG_SCRIPTS/node1/customBin/"

# --- Launch the shaded JAR ------------------------------------------------
#   • Adjust -Xms / -Xmx to the server’s RAM
#   • --activeProcessorCount is optional; leave it if you know the core count
java \
  -Dfile.encoding=UTF-8 \
  -Xms4g  -Xmx32g \
  -XX:ActiveProcessorCount=$(nproc) \
  -jar  "$MIG_SCRIPTS/target/batelco-migration-1.0-SNAPSHOT-jar-with-dependencies.jar" \
  Filter 1Step Initial 1

###############################################################################
# End of script
###############################################################################
