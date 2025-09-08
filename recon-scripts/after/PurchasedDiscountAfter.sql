-- DROP TABLE RECON_PD_AFTER PURGE;
CREATE TABLE RECON_PD_AFTER AS
SELECT 'After Hierarchy'  AS Category,
       'No of Purchased Discount (Group by Account, Product status)' AS Criteria,
       a.account_no AS Account,
       d.status,
       COUNT(*)     AS RecordCount
FROM   purchased_discount_t d,
       account_t           a
WHERE  a.poid_id0 = d.account_obj_id0
GROUP  BY a.account_no, d.status;
