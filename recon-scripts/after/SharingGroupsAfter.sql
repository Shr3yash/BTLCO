-- DROP TABLE RECON_SG_AFTER PURGE;
CREATE TABLE RECON_SG_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Sharing Groups' AS Criteria,
       COUNT(*) AS RecordCount
FROM   group_t g
WHERE  g.poid_type = '/group/sharing';
