-- ============================================
-- BEFORE HIERARCHY RECON (Pre-Migration)
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
--   drop_if_exists('RECON_CUSTOMER_BEFORE');
--   drop_if_exists('RECON_BILLING_BEFORE');
--   drop_if_exists('RECON_PROFILES_BEFORE');
--   drop_if_exists('RECON_INVOICE_BEFORE');
--   drop_if_exists('RECON_BAL_BEFORE');
--   drop_if_exists('RECON_SVC_BEFORE');
--   drop_if_exists('RECON_DEVICE_SIM_BEFORE');
--   drop_if_exists('RECON_SUSP_CDR_BEFORE');
--   drop_if_exists('RECON_PAYMENTS_BEFORE');
--   drop_if_exists('RECON_DEPT_BEFORE');
--   drop_if_exists('RECON_SA_BEFORE');
--   drop_if_exists('RECON_BILLINFO_BEFORE');
--   drop_if_exists('RECON_PP_BEFORE');
--   drop_if_exists('RECON_DEVICE_BEFORE');
--   drop_if_exists('RECON_DEVICE_IP_BEFORE');
--   drop_if_exists('RECON_ADJ_BEFORE');
--   drop_if_exists('RECON_ITEMS_BEFORE');
--   drop_if_exists('RECON_PD_BEFORE');
--   drop_if_exists('RECON_DEVICENUM_BEFORE');
--   drop_if_exists('RECON_CA_BEFORE');
--   drop_if_exists('RECON_RATED_EVT_BEFORE');
--   drop_if_exists('RECON_SG_BEFORE');
-- END;
-- /

-- Customer Accounts
CREATE TABLE RECON_CUSTOMER_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Customer Accounts in account_t' AS Criteria,
       COUNT(*) AS RecordCount
FROM   account_t a
WHERE  a.aac_access = 'Customer';

-- Billing Accounts
CREATE TABLE RECON_BILLING_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Billing Accounts in account_t' AS Criteria,
       COUNT(*) AS RecordCount
FROM   account_t a
WHERE  a.aac_access = 'Billing';

-- Profiles
CREATE TABLE RECON_PROFILES_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Profiles'    AS Criteria,
       COUNT(*)            AS RecordCount
FROM   profile_t;

-- Overall Invoice
CREATE TABLE RECON_INVOICE_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Overall Invoice' AS Criteria,
       COUNT(*) AS RecordCount
FROM   invoice_t;

-- Balances (by Account, Billinfo, Resource)
CREATE TABLE RECON_BAL_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
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
CREATE TABLE RECON_SVC_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Services (Group by Account, Service status)' AS Criteria,
       a.account_no AS Account,
       s.status     AS Status,
       COUNT(*)     AS RecordCount
FROM   service_t s
JOIN   account_t a ON a.poid_id0 = s.account_obj_id0
GROUP  BY a.account_no, s.status;

-- Device SIM (by status via device_t.state_id)
CREATE TABLE RECON_DEVICE_SIM_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Device_sim_t (Group by status)' AS Criteria,
       b.state_id AS State_Id,
       COUNT(*)   AS RecordCount
FROM   device_sim_t a
JOIN   device_t     b ON b.poid_id0 = a.obj_id0
GROUP  BY b.state_id;

-- Suspended CDRs
CREATE TABLE RECON_SUSP_CDR_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'Suspended CDRs '   AS Criteria,
       COUNT(*)            AS RecordCount
FROM   suspended_usage_t
WHERE  status = 0;

-- Payments (by Account)
CREATE TABLE RECON_PAYMENTS_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'Payments (Group by Account)' AS Criteria,
       a.account_no      AS Account,
       SUM(i.item_total) AS Payment
FROM   item_t i
JOIN   account_t a ON a.poid_id0 = i.account_obj_id0
WHERE  i.poid_type = '/item/payment'
GROUP  BY a.account_no;

-- Department Accounts (staging)
CREATE TABLE RECON_DEPT_BEFORE AS
SELECT 'Flat Hierarchy' AS Category,
       'No of Department Accounts in stg_dept_acct_t' AS Criteria,
       COUNT(*) AS RecordCount
FROM   stg_dept_acct_t
WHERE  aac_access = 'Department';

-- Service Accounts (staging)
CREATE TABLE RECON_SA_BEFORE AS
SELECT 'Flat Hierarchy' AS Category,
       'No of Service Accounts in stg_srvc_acct_t' AS Criteria,
       COUNT(*) AS RecordCount
FROM   stg_srvc_acct_t
WHERE  aac_access = 'Service';

-- Billinfo (overall)
CREATE TABLE RECON_BILLINFO_BEFORE AS
SELECT 'Flat Hierarchy' AS Category,
       'No of Billinfo'  AS Criteria,
       COUNT(*)          AS RecordCount
FROM   billinfo_t;

-- Purchased Product (by Account, Status)
CREATE TABLE RECON_PP_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Purchased Product (Group by Account, Product status)' AS Criteria,
       a.account_no AS Account,
       p.status     AS Status,
       COUNT(*)     AS RecordCount
FROM   purchased_product_t p
JOIN   account_t          a ON a.poid_id0 = p.account_obj_id0
GROUP  BY a.account_no, p.status;

-- Device (by status)
CREATE TABLE RECON_DEVICE_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Device_t (Group by status)' AS Criteria,
       state_id AS State_Id,
       COUNT(*) AS RecordCount
FROM   device_t
GROUP  BY state_id;

-- Device IP (by status via device_t.state_id)
CREATE TABLE RECON_DEVICE_IP_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Device_IP_t (Group by status)' AS Criteria,
       b.state_id AS State_Id,
       COUNT(*)   AS RecordCount
FROM   device_ip_t a
JOIN   device_t    b ON b.poid_id0 = a.obj_id0
GROUP  BY b.state_id;

-- Adjustments (by Account)
CREATE TABLE RECON_ADJ_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'Adjustments (Group by Account)' AS Criteria,
       a.account_no      AS Account,
       SUM(i.item_total) AS Adjustment
FROM   item_t i
JOIN   account_t a ON a.poid_id0 = i.account_obj_id0
WHERE  i.poid_type = '/item/adjustment'
GROUP  BY a.account_no;

-- Items (by Account, Item Type, Item Status)
CREATE TABLE RECON_ITEMS_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Items (Group by Account, item type, item status)' AS Criteria,
       a.account_no AS Account,
       i.poid_type  AS Poid_Type,
       i.status     AS Status,
       COUNT(*)     AS RecordCount
FROM   item_t i
JOIN   account_t a ON a.poid_id0 = i.account_obj_id0
GROUP  BY a.account_no, i.poid_type, i.status;

-- Purchased Discount (by Account, Product status)
CREATE TABLE RECON_PD_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Purchased Discount (Group by Account, Product status)' AS Criteria,
       a.account_no AS Account,
       d.status     AS Status,
       COUNT(*)     AS RecordCount
FROM   purchased_discount_t d
JOIN   account_t           a ON a.poid_id0 = d.account_obj_id0
GROUP  BY a.account_no, d.status;

-- Device_Num (by status)
CREATE TABLE RECON_DEVICENUM_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Device_Num_t (Group by status)' AS Criteria,
       dn.old_state_id AS Status,
       COUNT(*)        AS RecordCount
FROM   device_num_t dn
GROUP  BY dn.old_state_id;

-- Collection Action (by Billinfo, Status)
CREATE TABLE RECON_CA_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'Collection actions (Group by billinfo,status)' AS Criteria,
       ca.billinfo_obj_id0 AS Billinfo,
       ca.status           AS Status,
       COUNT(*)            AS RecordCount
FROM   collections_action_t ca
GROUP  BY ca.billinfo_obj_id0, ca.status;

-- Overall Rated Event (Completed)
CREATE TABLE RECON_RATED_EVT_BEFORE AS
SELECT 'Before Hierarchy'       AS Category,
       'Rated Event (Completed)' AS Criteria,
       COUNT(*)                 AS RecordCount
FROM   batch_t
WHERE  poid_type = '/batch/rel'
AND    status = 0;

-- Sharing Groups (common model: /group/sharing)
CREATE TABLE RECON_SG_BEFORE AS
SELECT 'Before Hierarchy' AS Category,
       'No of Sharing Groups' AS Criteria,
       COUNT(*) AS RecordCount
FROM   group_t g
WHERE  g.poid_type = '/group/sharing';
