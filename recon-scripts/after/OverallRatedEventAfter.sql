-- DROP TABLE RECON_RATED_EVT_AFTER PURGE;
CREATE TABLE RECON_RATED_EVT_AFTER AS
SELECT 'After Hierarchy'  AS Category,
       'Rated Event (Completed)' AS Criteria,
       COUNT(*) AS RecordCount
FROM   batch_t
WHERE  poid_type = '/batch/rel'
AND    status = 0;
