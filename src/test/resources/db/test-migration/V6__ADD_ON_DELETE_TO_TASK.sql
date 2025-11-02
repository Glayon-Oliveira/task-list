ALTER TABLE tasks
ADD CONSTRAINT fk_tasks_user_cascade
FOREIGN KEY (user_id) REFERENCES users(id)
ON DELETE CASCADE;