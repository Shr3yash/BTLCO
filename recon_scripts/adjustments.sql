WITH b AS (
  SELECT a.account_no, SUM(NVL(i.item_total,0)) amt
  FROM &&LEG..item_t i JOIN &&LEG..account_t a ON a.poid_id0 = i.account_obj_id0
  WHERE i.poid_type = '/item/adjustment'
  GROUP BY a.account_no
),
a AS (
  SELECT a.account_no, SUM(NVL(i.item_total,0)) amt
  FROM &&UPG..item_t i JOIN &&UPG..account_t a ON a.poid_id0 = i.account_obj_id0
  WHERE i.poid_type = '/item/adjustment'
  GROUP BY a.account_no
)
SELECT '&RUN_ID' run_id,
       COALESCE(b.account_no,a.account_no) account_no,
       NVL(b.amt,0) legacy_amt, NVL(a.amt,0) upgrade_amt,
       NVL(a.amt,0)-NVL(b.amt,0) delta_amt
FROM b FULL OUTER JOIN a USING (account_no)
WHERE ABS(NVL(a.amt,0)-NVL(b.amt,0)) > 0.01
ORDER BY account_no;
