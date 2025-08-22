WITH b AS (
  SELECT billinfo_obj_id0, status, COUNT(*) cnt
  FROM &&LEG..collections_action_t
  GROUP BY billinfo_obj_id0, status
),
a AS (
  SELECT billinfo_obj_id0, status, COUNT(*) cnt
  FROM &&UPG..collections_action_t
  GROUP BY billinfo_obj_id0, status
)
SELECT '&RUN_ID' run_id,
       COALESCE(b.billinfo_obj_id0,a.billinfo_obj_id0) billinfo_obj_id0,
       COALESCE(b.status,a.status) status,
       NVL(b.cnt,0) legacy_cnt, NVL(a.cnt,0) upgrade_cnt,
       NVL(a.cnt,0)-NVL(b.cnt,0) delta_cnt
FROM b FULL OUTER JOIN a
  ON NVL(b.billinfo_obj_id0,-1) = NVL(a.billinfo_obj_id0,-1)
 AND NVL(b.status,-1) = NVL(a.status,-1)
WHERE NVL(a.cnt,0) <> NVL(b.cnt,0)
ORDER BY billinfo_obj_id0, status;
