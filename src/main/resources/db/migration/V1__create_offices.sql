CREATE TABLE offices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    office_number VARCHAR(20) NOT NULL UNIQUE,
    square_feet INT,
    monthly_rent DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    description VARCHAR(255)
);