ALTER TABLE tasks ADD COLUMN user_id_new INT;

UPDATE tasks SET user_id_new = user_id;

ALTER TABLE tasks
ADD CONSTRAINT fk_tasks_user
FOREIGN KEY (user_id_new) REFERENCES users(id)
ON DELETE CASCADE;

ALTER TABLE tasks DROP COLUMN user_id;

ALTER TABLE tasks ALTER COLUMN user_id_new RENAME TO user_id;
