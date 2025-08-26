-- DROP TABLE RECON_SG_BEFORE PURGE;
CREATE TABLE RECON_SG_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Sharing Groups' AS Criteria,
       COUNT(*) AS RecordCount
FROM   group_t g
WHERE  g.poid_type = '/group/sharing';
