-- DROP TABLE RECON_OVERALL_BILL_BEFORE PURGE;
CREATE TABLE RECON_OVERALL_BILL_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Overall Bill' AS Criteria,
       COUNT(*) AS RecordCount
FROM   bill_t;
