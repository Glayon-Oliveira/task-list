ALTER TABLE subtasks
MODIFY COLUMN position DECIMAL(20, 10) NOT NULL;

UPDATE subtasks
SET position = position * 1024;