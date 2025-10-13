before and after hierarchy recon scripts modified\
What changed:

Added a safe “create-if-not-exists” block for RECON_FILTER_ACCOUNTS so you can run for all accounts by default or restrict to a custom list by inserting rows.

Added new recon outputs:

RECON_ACCT_BILL_SVC_PP_BEFORE

RECON_ACCT_BILL_SVC_PP_AFTER

Each block auto-drops the output table if it exists, then recreates it.

Filtering logic: runs for ALL accounts if RECON_FILTER_ACCOUNTS is empty; otherwise only for the listed account_nos.
