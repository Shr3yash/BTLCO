# DSG Script 

Per-group automation scripts for **DSG** post-migration and **DSG revert** flows.

> **Important:** if you want to run this properluy on server place your existing `common.sh` inside `dsg_pack/dsg_scripts/`.
> All scripts do:
> ```bash
> source "${SCRIPT_DIR}/common.sh"
> ```
> and expect `run_sql "<suite>" "<id>" "$SQL_BLOCK"` to be defined.

## Structure

## Usage
```bash
cd dsg_pack/dsg_scripts
cp /path/to/common.sh ./common.sh
chmod +x *.sh

# Run a range (inclusive):
./run_range.sh dsg 03 07
./run_range.sh dsg_revert 01 02

# Or run all:
./run_all_dsg.sh
./run_all_dsg_revert.sh
Typical usage inside your task scripts
source ./common.sh
run_sql "dsg" "04" "exec post_mig_insert_temp_group_sharing_discounts;"


The output looks like:

 dsg-04 completed — log: logs/dsg-04_20251113_154201.log


and if anything fails, you’ll get:

 dsg-04 failed — check logs/dsg-04_20251113_154201.log




 Setup

export CONF_FILE=/abspnshare/migration/brm/landing_dir/master.config
cd dsg_pack/dsg_scripts


Run a script

./dsg_04.sh


Or directly invoke

source ./common.sh
run_sql "dsg" "04" "exec post_mig_insert_temp_group_sharing_discounts;"


Outputs

dsg-04 completed successfully. Log: logs/dsg-04_20251113_152901.log