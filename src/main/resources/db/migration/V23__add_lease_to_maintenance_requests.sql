ALTER TABLE maintenance_requests
ADD COLUMN lease_id BIGINT NULL;

ALTER TABLE maintenance_requests
ADD CONSTRAINT fk_maintenance_request_lease
FOREIGN KEY (lease_id)
REFERENCES leases(id);