-- DROP TABLE RECON_CA_AFTER PURGE;
CREATE TABLE RECON_CA_AFTER AS
SELECT 'After Hierarchy'  AS Category,
       'Collection actions (Group by billinfo,status)' AS Criteria,
       ca.billinfo_obj_id0 AS Billinfo,
       ca.status           AS Status,
       COUNT(*)            AS RecordCount
FROM   collections_action_t ca
GROUP  BY ca.billinfo_obj_id0, ca.status;
