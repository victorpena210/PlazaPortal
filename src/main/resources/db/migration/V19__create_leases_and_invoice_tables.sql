CREATE TABLE leases (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT,
    office_id BIGINT,
    monthly_rent DECIMAL(10,2),
    start_date DATE,
    end_date DATE,
    active BIT,

    PRIMARY KEY (id),

    CONSTRAINT fk_lease_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES users(id),

    CONSTRAINT fk_lease_office
        FOREIGN KEY (office_id)
        REFERENCES offices(id)
);

CREATE TABLE invoice (
    id BIGINT NOT NULL AUTO_INCREMENT,
    lease_id BIGINT,
    amount DECIMAL(10,2),
    due_date DATE,
    status VARCHAR(50),
    created_at DATETIME,

    PRIMARY KEY (id),

    CONSTRAINT fk_invoice_lease
        FOREIGN KEY (lease_id)
        REFERENCES leases(id)
);