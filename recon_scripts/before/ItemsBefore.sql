-- DROP TABLE RECON_ITEMS_BEFORE PURGE;
CREATE TABLE RECON_ITEMS_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Items (Group by Account, item type, item status)' AS Criteria,
       a.account_no         AS Account,
       i.poid_type          AS Poid_Type,
       i.status             AS Status,
       COUNT(*)             AS RecordCount
FROM   item_t i,
       account_t a
WHERE  a.poid_id0 = i.account_obj_id0
GROUP  BY a.account_no, i.poid_type, i.status;
