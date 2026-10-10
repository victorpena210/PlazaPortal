CREATE TABLE companies (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(150) NOT NULL, slug VARCHAR(100) NOT NULL UNIQUE, support_email VARCHAR(255));
INSERT INTO companies(name,slug) VALUES ('Peña Plaza','pena-plaza');
CREATE TABLE company_memberships (id BIGINT PRIMARY KEY AUTO_INCREMENT, company_id BIGINT NOT NULL, user_id BIGINT NOT NULL, role VARCHAR(20) NOT NULL, CONSTRAINT uk_company_member UNIQUE(company_id,user_id), FOREIGN KEY(company_id) REFERENCES companies(id), FOREIGN KEY(user_id) REFERENCES users(id));
INSERT INTO company_memberships(company_id,user_id,role) SELECT c.id,u.id,CASE WHEN u.role='ADMIN' THEN 'OWNER' ELSE 'MANAGER' END FROM users u CROSS JOIN companies c WHERE c.slug='pena-plaza' AND u.role IN ('ADMIN','AGENT');
CREATE TABLE properties (id BIGINT PRIMARY KEY AUTO_INCREMENT, company_id BIGINT NOT NULL, name VARCHAR(150) NOT NULL, address VARCHAR(500) NOT NULL, active BIT NOT NULL DEFAULT 1, FOREIGN KEY(company_id) REFERENCES companies(id));
INSERT INTO properties(company_id,name,address,active) SELECT id,'Peña Plaza','Update the property address before launch',1 FROM companies WHERE slug='pena-plaza';
ALTER TABLE offices MODIFY monthly_rent DECIMAL(10,2) NULL;
ALTER TABLE offices ADD COLUMN property_id BIGINT NULL;
UPDATE offices SET property_id=(SELECT MIN(id) FROM properties) WHERE property_id IS NULL;
ALTER TABLE offices MODIFY property_id BIGINT NOT NULL;
ALTER TABLE offices ADD CONSTRAINT fk_office_property FOREIGN KEY(property_id) REFERENCES properties(id);
ALTER TABLE offices DROP INDEX office_number;
ALTER TABLE offices ADD CONSTRAINT uk_property_unit UNIQUE(property_id,office_number);
ALTER TABLE leases ADD COLUMN due_day INT NOT NULL DEFAULT 1, ADD COLUMN prorate_first_month BIT NOT NULL DEFAULT 0, ADD COLUMN grace_days INT NOT NULL DEFAULT 0, ADD COLUMN late_fee DECIMAL(10,2) NOT NULL DEFAULT 0;
ALTER TABLE invoice ADD COLUMN billing_period DATE NULL, ADD COLUMN kind VARCHAR(20) NOT NULL DEFAULT 'RENT', ADD COLUMN description VARCHAR(255), ADD COLUMN late_fee_amount DECIMAL(10,2) NOT NULL DEFAULT 0;
UPDATE invoice SET description=CONCAT('Rent · ',DATE_FORMAT(due_date,'%Y-%m'));
-- Preserve every historic invoice; only the earliest invoice in a lease/month
-- claims the billing period. Any pre-existing duplicates remain visible for review.
UPDATE invoice i JOIN (SELECT MIN(id) AS id, DATE_FORMAT(due_date,'%Y-%m-01') AS period FROM invoice WHERE due_date IS NOT NULL GROUP BY lease_id,DATE_FORMAT(due_date,'%Y-%m-01')) x ON i.id=x.id SET i.billing_period=x.period;
ALTER TABLE invoice ADD CONSTRAINT uk_invoice_period UNIQUE(lease_id,billing_period,kind);
ALTER TABLE payments MODIFY payment_month VARCHAR(7) NULL, MODIFY user_id BIGINT NULL, MODIFY status VARCHAR(30) NOT NULL;
ALTER TABLE payments ADD COLUMN refunded_amount DECIMAL(10,2) NOT NULL DEFAULT 0, ADD COLUMN attempt_key VARCHAR(64) UNIQUE, ADD COLUMN stripe_account_id VARCHAR(255), ADD COLUMN checkout_url VARCHAR(2048), ADD COLUMN checkout_expires_at DATETIME, ADD COLUMN note VARCHAR(500);
UPDATE payments SET total_amount=amount+COALESCE(late_fee,0) WHERE total_amount IS NULL;
CREATE INDEX idx_payment_session ON payments(stripe_session_id);
CREATE INDEX idx_payment_intent ON payments(stripe_payment_intent_id);
CREATE TABLE audit_entries (id BIGINT PRIMARY KEY AUTO_INCREMENT,company_id BIGINT NOT NULL,actor VARCHAR(255) NOT NULL,action VARCHAR(80) NOT NULL,details VARCHAR(1000) NOT NULL,created_at DATETIME NOT NULL,FOREIGN KEY(company_id) REFERENCES companies(id));
CREATE INDEX idx_audit_company ON audit_entries(company_id,created_at);
CREATE TABLE mail_outbox (id BIGINT PRIMARY KEY AUTO_INCREMENT,recipient VARCHAR(255) NOT NULL,subject VARCHAR(255) NOT NULL,body TEXT NOT NULL,attempts INT NOT NULL DEFAULT 0,created_at DATETIME NOT NULL,sent_at DATETIME,next_attempt_at DATETIME NOT NULL);
CREATE TABLE password_reset_tokens (id BIGINT PRIMARY KEY AUTO_INCREMENT,user_id BIGINT NOT NULL,token_hash VARCHAR(64) NOT NULL UNIQUE,expires_at DATETIME NOT NULL,used BIT NOT NULL DEFAULT 0,FOREIGN KEY(user_id) REFERENCES users(id));
CREATE TABLE membership_invitations (id BIGINT PRIMARY KEY AUTO_INCREMENT,company_id BIGINT NOT NULL,email VARCHAR(255) NOT NULL,role VARCHAR(20) NOT NULL,token_hash VARCHAR(64) NOT NULL UNIQUE,expires_at DATETIME NOT NULL,used BIT NOT NULL DEFAULT 0,FOREIGN KEY(company_id) REFERENCES companies(id));
CREATE TABLE stored_documents (id BIGINT PRIMARY KEY AUTO_INCREMENT,company_id BIGINT NOT NULL,lease_id BIGINT NOT NULL,original_name VARCHAR(255) NOT NULL,storage_key VARCHAR(64) NOT NULL UNIQUE,content_type VARCHAR(100) NOT NULL,size_bytes BIGINT NOT NULL,created_at DATETIME NOT NULL,FOREIGN KEY(company_id) REFERENCES companies(id),FOREIGN KEY(lease_id) REFERENCES leases(id));
CREATE TABLE processed_webhooks (id VARCHAR(255) PRIMARY KEY,processed_at DATETIME);
-- Invalidate old raw invitation tokens. Resend from the lease list.
UPDATE tenant_invitation SET used=1 WHERE used=0;
