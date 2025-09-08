-- DROP TABLE RECON_BILLING_AFTER PURGE;
CREATE TABLE RECON_BILLING_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Billing Accounts in account_t' AS Criteria,
       COUNT(*) AS RecordCount
FROM   account_t a
WHERE  a.aac_access = 'Billing';
