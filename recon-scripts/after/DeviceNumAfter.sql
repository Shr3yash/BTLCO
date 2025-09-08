-- DROP TABLE RECON_DEVICENUM_AFTER PURGE;
CREATE TABLE RECON_DEVICENUM_AFTER AS
SELECT 'After Hierarchy'  AS Category,
       'No of Device_Num_t (Group by status)' AS Criteria,
       dn.OLD_STATE_ID      AS Status,
       COUNT(*)             AS RecordCount
FROM   device_num_t dn
GROUP  BY dn.OLD_STATE_ID;
