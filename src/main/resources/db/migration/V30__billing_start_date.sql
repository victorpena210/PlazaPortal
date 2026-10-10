-- Preserve existing invoices, without inventing years of historical arrears on upgrade.
ALTER TABLE leases ADD COLUMN billing_start_date DATE NULL;
UPDATE leases SET billing_start_date=GREATEST(CAST(DATE_FORMAT(COALESCE(start_date,CURRENT_DATE),'%Y-%m-01') AS DATE),CAST(DATE_FORMAT(CURRENT_DATE,'%Y-%m-01') AS DATE));
