ALTER TABLE payments
ADD COLUMN lease_id BIGINT NULL;

ALTER TABLE payments
ADD CONSTRAINT fk_payments_lease
FOREIGN KEY (lease_id)
REFERENCES leases(id);