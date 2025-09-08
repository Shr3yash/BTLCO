-- DROP TABLE RECON_DEVICENUM_BEFORE PURGE;
CREATE TABLE RECON_DEVICENUM_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Device_Num_t (Group by status)' AS Criteria,
       dn.OLD_STATE_ID      AS Status,
       COUNT(*)             AS RecordCount
FROM   device_num_t dn
GROUP  BY dn.OLD_STATE_ID;
