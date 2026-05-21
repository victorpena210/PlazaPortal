CREATE INDEX idx_payments_office_id
ON payments (office_id);

ALTER TABLE payments
DROP INDEX uk_payment_office_month;