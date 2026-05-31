ALTER TABLE payments
ADD COLUMN invoice_id BIGINT;

ALTER TABLE payments
ADD CONSTRAINT fk_payment_invoice
FOREIGN KEY (invoice_id)
REFERENCES invoice(id);