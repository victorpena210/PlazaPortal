CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    paid_at DATETIME NOT NULL,
    user_id BIGINT NOT NULL,
    office_id BIGINT NOT NULL,
    CONSTRAINT fk_payments_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_payments_office
        FOREIGN KEY (office_id) REFERENCES offices(id)
);