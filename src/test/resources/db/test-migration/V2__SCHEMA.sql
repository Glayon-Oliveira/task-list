-- Add audit columns (created_at, updated_at) to "users" table
ALTER TABLE users
ADD created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE users
ADD updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Standardize audit columns in "tasks" table
ALTER TABLE tasks
ALTER COLUMN timestamp RENAME TO created_at;

ALTER TABLE tasks
ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE tasks
ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE tasks
ADD updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add "deadline" and "deadline_zone" to "tasks" table
ALTER TABLE tasks
ADD deadline TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE tasks
ALTER COLUMN deadline DROP DEFAULT;

ALTER TABLE tasks
ADD deadline_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';

ALTER TABLE tasks
ALTER COLUMN deadline_zone DROP DEFAULT;

-- Rename "task" column and update its structure
ALTER TABLE tasks
ALTER COLUMN task SET NULL;

ALTER TABLE tasks
ALTER COLUMN task RENAME TO summary;

-- Create "subtasks" table with relation to "tasks"
CREATE TABLE subtasks(

id INT PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(255) NOT NULL,
summary TEXT NULL,
duration_minutes INT DEFAULT 5,
status VARCHAR(30) NOT NULL,
position INT NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
task_id INT NOT NULL,

UNIQUE(task_id, position),
FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE

);
