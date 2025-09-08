-- DROP TABLE RECON_ITEMS_AFTER PURGE;
CREATE TABLE RECON_ITEMS_AFTER AS
SELECT 'After Hierarchy'  AS Category,
       'No of Items (Group by Account, item type, item status)' AS Criteria,
       a.account_no         AS Account,
       i.poid_type          AS Poid_Type,
       i.status             AS Status,
       COUNT(*)             AS RecordCount
FROM   item_t i,
       account_t a
WHERE  a.poid_id0 = i.account_obj_id0
GROUP  BY a.account_no, i.poid_type, i.status;
