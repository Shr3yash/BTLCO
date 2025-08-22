WITH b AS (
  SELECT a.account_no, bg.billinfo_obj_id0 billinfo_obj_id0, s.rec_id2 resource,
         SUM(NVL(s.current_bal,0)) bal
  FROM &&LEG..bal_grp_t bg
  JOIN &&LEG..bal_grp_sub_bals_t s ON s.obj_id0 = bg.poid_id0
  JOIN &&LEG..account_t a ON a.poid_id0 = bg.account_obj_id0
  GROUP BY a.account_no, bg.billinfo_obj_id0, s.rec_id2
),
a AS (
  SELECT a.account_no, bg.billinfo_obj_id0 billinfo_obj_id0, s.rec_id2 resource,
         SUM(NVL(s.current_bal,0)) bal
  FROM &&UPG..bal_grp_t bg
  JOIN &&UPG..bal_grp_sub_bals_t s ON s.obj_id0 = bg.poid_id0
  JOIN &&UPG..account_t a ON a.poid_id0 = bg.account_obj_id0
  GROUP BY a.account_no, bg.billinfo_obj_id0, s.rec_id2
)
SELECT '&RUN_ID' run_id,
       COALESCE(b.account_no,a.account_no) account_no,
       COALESCE(b.billinfo_obj_id0,a.billinfo_obj_id0) billinfo_obj_id0,
       COALESCE(b.resource,a.resource) resource,
       NVL(b.bal,0) legacy_bal, NVL(a.bal,0) upgrade_bal,
       NVL(a.bal,0)-NVL(b.bal,0) delta_bal
FROM b FULL OUTER JOIN a
  ON b.account_no = a.account_no
 AND NVL(b.billinfo_obj_id0,-1) = NVL(a.billinfo_obj_id0,-1)
 AND NVL(b.resource,-1) = NVL(a.resource,-1)
WHERE ABS(NVL(a.bal,0)-NVL(b.bal,0)) > 0.01
ORDER BY account_no, billinfo_obj_id0, resource;
