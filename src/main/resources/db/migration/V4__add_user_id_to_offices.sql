ALTER TABLE offices
ADD COLUMN user_id BIGINT NULL;

ALTER TABLE offices
ADD CONSTRAINT fk_offices_user
FOREIGN KEY (user_id) REFERENCES users(id);