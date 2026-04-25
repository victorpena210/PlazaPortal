ALTER TABLE payments 
ADD COLUMN payments_month VARCHAR(7) NOT NULL AFTER status;

ALTER TABLE payments
ADD CONSTRAINT uk_payment_office_month
UNIQUE (office_id, payment_month);