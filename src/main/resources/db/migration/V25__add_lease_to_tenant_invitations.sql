ALTER TABLE tenant_invitation
ADD CONSTRAINT fk_invitation_lease
FOREIGN KEY (lease_id)
REFERENCES leases(id);