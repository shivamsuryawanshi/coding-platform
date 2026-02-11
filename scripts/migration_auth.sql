-- ============================================
-- Migration Script: Add Auth Support
-- Run this AFTER schema.sql on existing database
-- ============================================

-- Add users table if not exists
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add user_id column to submissions if not exists
ALTER TABLE submissions 
ADD COLUMN IF NOT EXISTS user_id BIGINT NULL AFTER id;

-- Add status column to submissions if not exists
ALTER TABLE submissions 
ADD COLUMN IF NOT EXISTS status ENUM('QUEUED','RUNNING','ACCEPTED','WRONG_ANSWER','TLE','RE','CE') DEFAULT 'QUEUED' AFTER code;

-- Add foreign key for user_id if not exists
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'submissions'
    AND CONSTRAINT_NAME = 'submissions_ibfk_1'
);

SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE submissions ADD FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE',
    'SELECT "Foreign key already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add indexes if not exists
CREATE INDEX IF NOT EXISTS idx_user_id ON submissions(user_id);
CREATE INDEX IF NOT EXISTS idx_status ON submissions(status);

-- Update existing submissions to have a default status based on verdict
UPDATE submissions 
SET status = 
    CASE 
        WHEN verdict = 'Accepted' THEN 'ACCEPTED'
        WHEN verdict = 'Wrong Answer' THEN 'WRONG_ANSWER'
        WHEN verdict = 'Time Limit Exceeded' THEN 'TLE'
        WHEN verdict = 'Runtime Error' THEN 'RE'
        WHEN verdict = 'Compilation Error' THEN 'CE'
        ELSE 'QUEUED'
    END
WHERE status IS NULL;

