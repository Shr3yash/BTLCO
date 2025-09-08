CREATE TABLE recon_purchased_product (
  run_id        VARCHAR2(100),       
  phase         VARCHAR2(20),        -- 'Before upgrade' / 'After upgrade'
  category      VARCHAR2(50),        -- fixed text
  criteria      VARCHAR2(200),       -- 
  account       VARCHAR2(100),
  status        NUMBER,              
  recordcount   NUMBER,
  run_ts        TIMESTAMP DEFAULT SYSTIMESTAMP
);

-- Helpful indexes 
CREATE INDEX ix_rpp_run ON recon_purchased_product (run_id, phase);
CREATE INDEX ix_rpp_acct_status ON recon_purchased_product (account, status);
