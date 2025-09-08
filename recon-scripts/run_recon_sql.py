#!/usr/bin/env python3
"""
run_recon_sql.py
Execute one or more SQL files against Oracle (dev2), matching team style.

Usage examples:
  python run_recon_sql.py 01_before_hierarchy.sql 02_after_hierarchy.sql
  python run_recon_sql.py -v --continue 01_before_hierarchy.sql
  python run_recon_sql.py --config /custom/master.config --cred-key PIN2_CREDENTIAL 01.sql
"""

import argparse
import os
import re
import sys
import time
from pathlib import Path

try:
    import oracledb  # pip install oracledb
except ImportError:
    print("ERROR: Please install oracledb (pip install oracledb)", file=sys.stderr)
    sys.exit(1)

DEFAULT_CONFIG = "/home/dev-user/azra/Cmt_Scripts/pod_cmt_scripts/master.config"
SQLPLUS_ONLY_PREFIXES = (
    "set ", "whenever ", "spool ", "column ", "col ", "prompt ",
    "alter session set ", "alter session enable ", "alter session force ",
    "define ", "undefine ", "@", "host ", "pause ", "ttitle ", "btitle ",
)

def parse_master_config(conf_path: Path) -> dict:
    kv = {}
    with conf_path.open("r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):  # skip comments/blank
                continue
            if "=" in line:
                k, v = line.split("=", 1)
                kv[k.strip()] = v.strip()
    return kv

def resolve_credential(kv: dict, cred_key: str) -> str:
    if cred_key not in kv:
        raise KeyError(f"Credential key '{cred_key}' not found in config.")

    cred = kv[cred_key]

    # Expand tokens like $DEV2_DB_IP from config or environment
    pattern = re.compile(r"\$(\w+)")
    def repl(m):
        var = m.group(1)
        return kv.get(var, os.environ.get(var, m.group(0)))
    cred = pattern.sub(repl, cred)

    return cred

def load_sql_file(path: Path) -> str:
    with path.open("r", encoding="utf-8") as f:
        return f.read()

def strip_sqlplus_lines(sql: str) -> str:
    out_lines = []
    for raw in sql.splitlines():
        line = raw.strip()
        if not line:
            out_lines.append("")
            continue
        low = line.lower()
        if any(low.startswith(p) for p in SQLPLUS_ONLY_PREFIXES):
            continue
        if line.startswith("--"):
            out_lines.append("")  # keep a blank separator
            continue
        out_lines.append(raw)
    return "\n".join(out_lines)

def split_statements(sql: str) -> list:
    statements = []
    buf = []
    in_single = False
    in_double = False

    for ch in sql:
        if ch == "'" and not in_double:
            in_single = not in_single
        elif ch == '"' and not in_single:
            in_double = not in_double

        if ch == ";" and not in_single and not in_double:
            stmt = "".join(buf).strip()
            if stmt:
                statements.append(stmt)
            buf = []
        else:
            buf.append(ch)

    tail = "".join(buf).strip()
    if tail:
        statements.append(tail)

    return [s for s in statements if s and not s.startswith("--")]

def execute_sql_file(conn, sql_path: Path, verbose=False, continue_on_error=False):
    sql_raw = load_sql_file(sql_path)
    sql_clean = strip_sqlplus_lines(sql_raw)
    statements = split_statements(sql_clean)

    if verbose:
        print(f"\n-- Executing {sql_path} with {len(statements)} statements --")

    cur = conn.cursor()
    ok = 0
    for idx, stmt in enumerate(statements, 1):
        t0 = time.time()
        try:
            if verbose:
                print(f"\n[{idx}/{len(statements)}] Running:\n{stmt[:4000]}")
            cur.execute(stmt)
            conn.commit()  # safe even for DDL
            ok += 1
            if verbose:
                print(f"✓ OK ({time.time()-t0:.2f}s)")
        except oracledb.DatabaseError as e:
            err = e.args[0]
            print(f"\n✗ ERROR in statement {idx}: {err}", file=sys.stderr)
            if not continue_on_error:
                raise
            elif verbose:
                print("…continuing on error…")
    return ok, len(statements)

def main():
    ap = argparse.ArgumentParser(description="Run recon SQL files on Oracle dev2")
    ap.add_argument("sql_files", nargs="+", help="Path(s) to .sql file(s) to execute in order")
    ap.add_argument("-c", "--config", default=DEFAULT_CONFIG,
                    help=f"Path to master.config (default: {DEFAULT_CONFIG})")
    ap.add_argument("-k", "--cred-key", default="PIN1_CREDENTIAL",
                    help="Key in master.config holding the EASY CONNECT string")
    ap.add_argument("--dsn", default=None,
                    help="Override DSN (host:port/service or full easy-connect). Use with --user/--password")
    ap.add_argument("-u", "--user", default=None, help="DB user (if not using cred-key)")
    ap.add_argument("-p", "--password", default=None, help="DB password (if not using cred-key)")
    ap.add_argument("-v", "--verbose", action="store_true", help="Verbose logging")
    ap.add_argument("--continue", dest="cont", action="store_true",
                    help="Continue on statement errors")
    args = ap.parse_args()

    conf_path = Path(args.config)
    if not conf_path.exists():
        print(f"ERROR: config file not found: {conf_path}", file=sys.stderr)
        sys.exit(2)

    kv = parse_master_config(conf_path)

    # Connect
    if args.user and args.password and args.dsn:
        dsn = args.dsn
        if "/" in dsn or "@" in dsn:
            conn_str = f"{args.user}/{args.password}@{dsn}"
            conn = oracledb.connect(conn_str, encoding="UTF-8")
        else:
            host, rest = dsn.split(":", 1)
            port, service = rest.split("/", 1)
            dsn_obj = oracledb.makedsn(host=host, port=int(port), service_name=service)
            conn = oracledb.connect(user=args.user, password=args.password, dsn=dsn_obj, encoding="UTF-8")
    else:
        cred = resolve_credential(kv, args.cred_key)
        conn = oracledb.connect(cred, encoding="UTF-8")

    total_ok = 0
    total_all = 0
    start = time.time()
    try:
        for f in args.sql_files:
            sql_path = Path(f)
            if not sql_path.exists():
                print(f"ERROR: SQL file not found: {sql_path}", file=sys.stderr)
                sys.exit(3)
            ok, all_ = execute_sql_file(conn, sql_path, verbose=args.verbose, continue_on_error=args.cont)
            total_ok += ok
            total_all += all_
    finally:
        conn.close()
    elapsed = time.time() - start
    print(f"\nDone. Executed {total_ok}/{total_all} statements successfully in {elapsed:.2f}s.")

if __name__ == "__main__":
    main()
