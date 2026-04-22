CREATE TABLE maintenance_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    user_id BIGINT NOT NULL,
    office_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_maintenance_request_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_maintenance_request_office
        FOREIGN KEY (office_id) REFERENCES offices(id)
);