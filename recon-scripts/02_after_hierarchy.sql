-- ============================================
-- AFTER HIERARCHY RECON (Post-Migration)
-- Style: Category + Criteria + counts only
-- Output: one CREATE TABLE per check
-- ============================================

-- Optional clean re-run helper (leave commented unless needed)
-- DECLARE
--   PROCEDURE drop_if_exists(p_tab VARCHAR2) IS
--   BEGIN
--     EXECUTE IMMEDIATE 'DROP TABLE '||p_tab||' PURGE';
--   EXCEPTION
--     WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF;
--   END;
-- BEGIN
--   drop_if_exists('RECON_CUSTOMER_AFTER');
--   drop_if_exists('RECON_BILLING_AFTER');
--   drop_if_exists('RECON_PROFILES_AFTER');
--   drop_if_exists('RECON_INVOICE_AFTER');
--   drop_if_exists('RECON_BAL_AFTER');
--   drop_if_exists('RECON_SVC_AFTER');
--   drop_if_exists('RECON_DEVICE_SIM_AFTER');
--   drop_if_exists('RECON_SUSP_CDR_AFTER');
--   drop_if_exists('RECON_PAYMENTS_AFTER');
--   drop_if_exists('RECON_DEPT_AFTER');
--   drop_if_exists('RECON_SA_AFTER');
--   drop_if_exists('RECON_BILLINFO_AFTER');
--   drop_if_exists('RECON_PP_AFTER');
--   drop_if_exists('RECON_DEVICE_AFTER');
--   drop_if_exists('RECON_DEVICE_IP_AFTER');
--   drop_if_exists('RECON_ADJ_AFTER');
--   drop_if_exists('RECON_ITEMS_AFTER');
--   drop_if_exists('RECON_PD_AFTER');
--   drop_if_exists('RECON_DEVICENUM_AFTER');
--   drop_if_exists('RECON_CA_AFTER');
--   drop_if_exists('RECON_RATED_EVT_AFTER');
--   drop_if_exists('RECON_SG_AFTER');
--   drop_if_exists('RECON_SVC_MIGRATED_AFTER');
-- END;
-- /

-- Customer Accounts
CREATE TABLE RECON_CUSTOMER_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Customer Accounts in account_t' AS Criteria,
       COUNT(*) AS RecordCount
FROM   account_t a
WHERE  a.aac_access = 'Customer';

-- Billing Accounts
CREATE TABLE RECON_BILLING_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Billing Accounts in account_t' AS Criteria,
       COUNT(*) AS RecordCount
FROM   account_t a
WHERE  a.aac_access = 'Billing';

-- Profiles
CREATE TABLE RECON_PROFILES_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Profiles'   AS Criteria,
       COUNT(*)           AS RecordCount
FROM   profile_t;

-- Overall Invoice
CREATE TABLE RECON_INVOICE_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Overall Invoice' AS Criteria,
       COUNT(*) AS RecordCount
FROM   invoice_t;

-- Balances (by Account, Billinfo, Resource)
CREATE TABLE RECON_BAL_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'Balances (Group by Account,billinfo,resource)' AS Criteria,
       a.account_no        AS Account,
       s.rec_id2           AS resources,
       bg.billinfo_obj_id0 AS billinfo_obj_id0,
       SUM(s.current_bal)  AS Balance
FROM   bal_grp_t bg
JOIN   bal_grp_sub_bals_t s ON s.obj_id0 = bg.poid_id0
JOIN   account_t a          ON a.poid_id0 = bg.account_obj_id0
GROUP  BY a.account_no, s.rec_id2, bg.billinfo_obj_id0;

-- Services (by Account, Status)
CREATE TABLE RECON_SVC_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Services (Group by Account, Service status)' AS Criteria,
       a.account_no AS Account,
       s.status     AS Status,
       COUNT(*)     AS RecordCount
FROM   service_t s
JOIN   account_t a ON a.poid_id0 = s.account_obj_id0
GROUP  BY a.account_no, s.status;

-- Device SIM (by status via device_t.state_id)
CREATE TABLE RECON_DEVICE_SIM_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Device_sim_t (Group by status)' AS Criteria,
       b.state_id AS State_Id,
       COUNT(*)   AS RecordCount
FROM   device_sim_t a
JOIN   device_t     b ON b.poid_id0 = a.obj_id0
GROUP  BY b.state_id;

-- Suspended CDRs
CREATE TABLE RECON_SUSP_CDR_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'Suspended CDRs '  AS Criteria,
       COUNT(*)           AS RecordCount
FROM   suspended_usage_t
WHERE  status = 0;

-- Payments (by Account)
CREATE TABLE RECON_PAYMENTS_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'Payments (Group by Account)' AS Criteria,
       a.account_no      AS Account,
       SUM(i.item_total) AS Payment
FROM   item_t i
JOIN   account_t a ON a.poid_id0 = i.account_obj_id0
WHERE  i.poid_type = '/item/payment'
GROUP  BY a.account_no;

-- Department Accounts (final in account_t)
CREATE TABLE RECON_DEPT_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Department Accounts in account_t' AS Criteria,
       COUNT(*) AS RecordCount
FROM   account_t a
WHERE  a.aac_access = 'Department';

-- Service Accounts (final in account_t)
CREATE TABLE RECON_SA_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Service Accounts in account_t' AS Criteria,
       COUNT(*) AS RecordCount
FROM   account_t a
WHERE  a.aac_access = 'Service';

-- Services migrated to service accounts
CREATE TABLE RECON_SVC_MIGRATED_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of services migrated to service account' AS Criteria,
       COUNT(*) AS RecordCount
FROM   service_t s
JOIN   account_t a ON a.poid_id0 = s.account_obj_id0
WHERE  a.aac_access = 'Service';

-- Billinfo (final; only for billing accounts)
CREATE TABLE RECON_BILLINFO_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Billinfo'   AS Criteria,
       COUNT(*)           AS RecordCount
FROM   billinfo_t b
JOIN   account_t  a ON a.poid_id0 = b.account_obj_id0
WHERE  a.aac_access = 'Billing';

-- Purchased Product (by Account, Status)
CREATE TABLE RECON_PP_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Purchased Product (Group by Account, Product status)' AS Criteria,
       a.account_no AS Account,
       p.status     AS Status,
       COUNT(*)     AS RecordCount
FROM   purchased_product_t p
JOIN   account_t          a ON a.poid_id0 = p.account_obj_id0
GROUP  BY a.account_no, p.status;

-- Device (by status)
CREATE TABLE RECON_DEVICE_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Device_t (Group by status)' AS Criteria,
       state_id AS State_Id,
       COUNT(*) AS RecordCount
FROM   device_t
GROUP  BY state_id;

-- Device IP (by status via device_t.state_id)
CREATE TABLE RECON_DEVICE_IP_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Device_IP_t (Group by status)' AS Criteria,
       b.state_id AS State_Id,
       COUNT(*)   AS RecordCount
FROM   device_ip_t a
JOIN   device_t    b ON b.poid_id0 = a.obj_id0
GROUP  BY b.state_id;

-- Adjustments (by Account)
CREATE TABLE RECON_ADJ_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'Adjustments (Group by Account)' AS Criteria,
       a.account_no      AS Account,
       SUM(i.item_total) AS Adjustment
FROM   item_t i
JOIN   account_t a ON a.poid_id0 = i.account_obj_id0
WHERE  i.poid_type = '/item/adjustment'
GROUP  BY a.account_no;

-- Items (by Account, Item Type, Item Status)
CREATE TABLE RECON_ITEMS_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Items (Group by Account, item type, item status)' AS Criteria,
       a.account_no AS Account,
       i.poid_type  AS Poid_Type,
       i.status     AS Status,
       COUNT(*)     AS RecordCount
FROM   item_t i
JOIN   account_t a ON a.poid_id0 = i.account_obj_id0
GROUP  BY a.account_no, i.poid_type, i.status;

-- Purchased Discount (by Account, Product status)
CREATE TABLE RECON_PD_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Purchased Discount (Group by Account, Product status)' AS Criteria,
       a.account_no AS Account,
       d.status     AS Status,
       COUNT(*)     AS RecordCount
FROM   purchased_discount_t d
JOIN   account_t           a ON a.poid_id0 = d.account_obj_id0
GROUP  BY a.account_no, d.status;

-- Device_Num (by status)
CREATE TABLE RECON_DEVICENUM_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Device_Num_t (Group by status)' AS Criteria,
       dn.old_state_id AS Status,
       COUNT(*)        AS RecordCount
FROM   device_num_t dn
GROUP  BY dn.old_state_id;

-- Collection Action (by Billinfo, Status)
CREATE TABLE RECON_CA_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'Collection actions (Group by billinfo,status)' AS Criteria,
       ca.billinfo_obj_id0 AS Billinfo,
       ca.status           AS Status,
       COUNT(*)            AS RecordCount
FROM   collections_action_t ca
GROUP  BY ca.billinfo_obj_id0, ca.status;

-- Overall Rated Event (Completed)
CREATE TABLE RECON_RATED_EVT_AFTER AS
SELECT 'After Hierarchy'        AS Category,
       'Rated Event (Completed)' AS Criteria,
       COUNT(*)                 AS RecordCount
FROM   batch_t
WHERE  poid_type = '/batch/rel'
AND    status = 0;

-- Sharing Groups (common model: /group/sharing)
CREATE TABLE RECON_SG_AFTER AS
SELECT 'After Hierarchy' AS Category,
       'No of Sharing Groups' AS Criteria,
       COUNT(*) AS RecordCount
FROM   group_t g
WHERE  g.poid_type = '/group/sharing';
