Reads DB creds from your master.config (path overridable via CONF_FILE).

Requires DB_USER, DB_PASS, and DB_TNS to be set there.

Logs to validationScripts/logs by default (override with LOG_DIR).

Exposes two helpers your scripts can call:

run_sql "validate" "07" "$SQL_BLOCK"

run_sql_file "validate" "03" "/path/to/file.sql"


this is what the common sh file is doing as of now.