-- ======= AFTER UPGRADE =======
DEFINE RUN_ID = 'AFTER_2025_08_25';
DEFINE PHASE  = 'After upgrade';

CREATE TABLE recon_purchased_product AS
SELECT '&RUN_ID' AS run_id,
       '&PHASE'  AS phase,
       'After upgrade' AS category,
       'No of Purchased Product (Group by Account, Product status)' AS criteria,
       a.account_no AS account,
       p.status,
       COUNT(*) AS recordcount
FROM   purchased_product_t p
JOIN   account_t          a ON a.poid_id0 = p.account_obj_id0
GROUP  BY a.account_no, p.status;
