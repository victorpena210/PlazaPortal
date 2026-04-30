ALTER TABLE payments
ADD COLUMN payment_month VARCHAR(7) NULL AFTER status;

UPDATE payments
SET payment_month = DATE_FORMAT(paid_at, '%Y-%m')
WHERE payment_month IS NULL;

ALTER TABLE payments
MODIFY payment_month VARCHAR(7) NOT NULL;

ALTER TABLE payments
ADD CONSTRAINT uk_payment_office_month
UNIQUE (office_id, payment_month);