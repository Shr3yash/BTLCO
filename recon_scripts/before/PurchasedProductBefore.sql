-- ======= BEFORE UPGRADE =======
DEFINE RUN_ID = 'BEFORE_2025_08_24';
DEFINE PHASE  = 'Before upgrade';

CREATE TABLE recon_purchased_product AS
SELECT '&RUN_ID' AS run_id,
       '&PHASE'  AS phase,
       'Before upgrade' AS category,
       'No of Purchased Product (Group by Account, Product status)' AS criteria,
       a.account_no AS account,
       p.status,
       COUNT(*) AS recordcount
FROM   purchased_product_t p
JOIN   account_t          a ON a.poid_id0 = p.account_obj_id0
GROUP  BY a.account_no, p.status;
