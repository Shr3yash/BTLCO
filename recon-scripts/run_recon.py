#!/usr/bin/env python3
import subprocess
import datetime
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CONF_FILE = os.path.join(BASE_DIR, "master.config")
SQL_BEFORE = os.path.join(BASE_DIR, "shryshSQL/01_before_hierarchy.sql")
SQL_AFTER = os.path.join(BASE_DIR, "shryshSQL/02_after_hierarchy.sql")

ts = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
out_file = os.path.join(BASE_DIR, f"shryshSQL/recon_results_{ts}.txt")

cmd_before = [os.path.join(BASE_DIR, "run_recon_before.sh"), CONF_FILE, SQL_BEFORE]
cmd_after = [os.path.join(BASE_DIR, "run_recon_after.sh"), CONF_FILE, SQL_AFTER]

with open(out_file, "w") as f:
    f.write("===== BEFORE RECON =====\n")
    subprocess.run(cmd_before, stdout=f, stderr=subprocess.STDOUT, check=True)
    f.write("\n===== AFTER RECON =====\n")
    subprocess.run(cmd_after, stdout=f, stderr=subprocess.STDOUT, check=True)

print(f"Recon results saved to {out_file}")
