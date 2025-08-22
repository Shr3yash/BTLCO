WITH b_dev AS (SELECT state_id status, COUNT(*) cnt FROM &&LEG..device_t GROUP BY state_id),
     a_dev AS (SELECT state_id status, COUNT(*) cnt FROM &&UPG..device_t GROUP BY state_id),
     b_num AS (SELECT old_state_id status, COUNT(*) cnt FROM &&LEG..device_num_t GROUP BY old_state_id),
     a_num AS (SELECT old_state_id status, COUNT(*) cnt FROM &&UPG..device_num_t GROUP BY old_state_id),
     b_sim AS (
       SELECT d.state_id status, COUNT(*) cnt
       FROM &&LEG..device_sim_t s JOIN &&LEG..device_t d ON d.poid_id0 = s.obj_id0
       GROUP BY d.state_id
     ),
     a_sim AS (
       SELECT d.state_id status, COUNT(*) cnt
       FROM &&UPG..device_sim_t s JOIN &&UPG..device_t d ON d.poid_id0 = s.obj_id0
       GROUP BY d.state_id
     ),
     b_ip AS (
       SELECT d.state_id status, COUNT(*) cnt
       FROM &&LEG..device_ip_t i JOIN &&LEG..device_t d ON d.poid_id0 = i.obj_id0
       GROUP BY d.state_id
     ),
     a_ip AS (
       SELECT d.state_id status, COUNT(*) cnt
       FROM &&UPG..device_ip_t i JOIN &&UPG..device_t d ON d.poid_id0 = i.obj_id0
       GROUP BY d.state_id
     )
SELECT '&RUN_ID' run_id, 'device_t' entity, COALESCE(b.status,a.status) status,
       NVL(b.cnt,0) legacy_cnt, NVL(a.cnt,0) upgrade_cnt, NVL(a.cnt,0)-NVL(b.cnt,0) delta_cnt
FROM b_dev b FULL JOIN a_dev a USING(status)
WHERE NVL(a.cnt,0) <> NVL(b.cnt,0)
UNION ALL
SELECT '&RUN_ID', 'device_num_t', COALESCE(b.status,a.status), NVL(b.cnt,0), NVL(a.cnt,0), NVL(a.cnt,0)-NVL(b.cnt,0)
FROM b_num b FULL JOIN a_num a USING(status)
WHERE NVL(a.cnt,0) <> NVL(b.cnt,0)
UNION ALL
SELECT '&RUN_ID', 'device_sim_t', COALESCE(b.status,a.status), NVL(b.cnt,0), NVL(a.cnt,0), NVL(a.cnt,0)-NVL(b.cnt,0)
FROM b_sim b FULL JOIN a_sim a USING(status)
WHERE NVL(a.cnt,0) <> NVL(b.cnt,0)
UNION ALL
SELECT '&RUN_ID', 'device_ip_t', COALESCE(b.status,a.status), NVL(b.cnt,0), NVL(a.cnt,0), NVL(a.cnt,0)-NVL(b.cnt,0)
FROM b_ip b FULL JOIN a_ip a USING(status)
WHERE NVL(a.cnt,0) <> NVL(b.cnt,0)
ORDER BY 1,2,3;
