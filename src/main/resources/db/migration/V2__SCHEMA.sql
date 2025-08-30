-- Add audit columns (created_at, updated_at) to "users" table
ALTER TABLE users
ADD created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER role;

ALTER TABLE users
ADD updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- Standardize audit columns in "tasks" table
ALTER TABLE tasks
CHANGE timestamp created_at TIMESTAMP NOT NULL;

ALTER TABLE tasks
MODIFY created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE tasks
ADD updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- Add "deadline" and "deadline_zone" to "tasks" table
ALTER TABLE tasks
ADD deadline TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER task;

ALTER TABLE tasks
ALTER COLUMN deadline DROP DEFAULT;

ALTER TABLE tasks
ADD deadline_zone VARCHAR(50) NOT NULL DEFAULT "UTC" AFTER deadline;

ALTER TABLE tasks
ALTER COLUMN deadline_zone DROP DEFAULT;

-- Rename "task" column and update its structure
ALTER TABLE tasks
MODIFY task TEXT NULL;

ALTER TABLE tasks
CHANGE task summary TEXT NULL;

-- Create "subtasks" table with relation to "tasks"
CREATE TABLE subtasks(

id INT PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(255) NOT NULL,
summary TEXT NULL,
duration_minutes INT DEFAULT 5,
status VARCHAR(30) NOT NULL,
position INT NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
task_id INT NOT NULL,

UNIQUE(task_id, position),
FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE

);
