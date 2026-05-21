ALTER TABLE maintenance_requests
ADD COLUMN priority VARCHAR(20),

ADD COLUMN image_url VARCHAR(255),

ADD COLUMN tenant_comment TEXT,

ADD COLUMN admin_note TEXT,

ADD COLUMN completed_at TIMESTAMP,

ADD COLUMN updated_at TIMESTAMP;