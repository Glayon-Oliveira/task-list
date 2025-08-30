-- Add audit columns (created_at, updated_at) to "users" table
ALTER TABLE users
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Standardize audit columns in "tasks" table
ALTER TABLE tasks CHANGE COLUMN `timestamp` created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE tasks
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Add "deadline" and "deadline_zone" to "tasks" table
ALTER TABLE tasks
ADD COLUMN deadline TIMESTAMP NULL,
ADD COLUMN deadline_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';

ALTER TABLE tasks
MODIFY deadline TIMESTAMP NULL,
MODIFY deadline_zone VARCHAR(50) NOT NULL;

-- Rename "task" column and update its structure
ALTER TABLE tasks CHANGE COLUMN task summary TEXT NULL;

-- Create "subtasks" table with relation to "tasks"
CREATE TABLE subtasks(
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    summary TEXT NULL,
    duration_minutes INT DEFAULT 5,
    status ENUM('PENDING','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'PENDING',
    position INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    task_id INT NOT NULL,
    UNIQUE(task_id, position),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);
