-- DROP TABLE RECON_OVERALL_BILL_AFTER PURGE;
CREATE TABLE RECON_OVERALL_BILL_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Overall Bill' AS Criteria,
       COUNT(*) AS RecordCount
FROM   bill_t;
