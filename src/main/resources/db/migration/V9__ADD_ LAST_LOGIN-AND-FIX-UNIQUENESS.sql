ALTER TABLE users
ADD COLUMN last_login TIMESTAMP NULL;

ALTER TABLE user_emails 
DROP FOREIGN KEY user_emails_ibfk_1;

ALTER TABLE user_emails 
DROP INDEX user_id;

ALTER TABLE user_emails
ADD CONSTRAINT fk_user_emails_user
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_emails ADD UNIQUE (email);