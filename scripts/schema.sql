-- ============================================
-- CodeNexus Database Schema
-- RDS MySQL 8.0 | Region: eu-north-1
-- ============================================

-- Run this on your RDS instance:
-- mysql -h <RDS_ENDPOINT> -u admin -p coding_platform < schema.sql

-- ============================================
-- Table: problems
-- Stores problem metadata from problem.json files
-- ============================================
CREATE TABLE IF NOT EXISTS problems (
    id VARCHAR(120) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    difficulty ENUM('easy', 'medium', 'hard') NOT NULL,
    statement TEXT NOT NULL,
    input_format TEXT,
    output_format TEXT,
    constraints TEXT,
    time_limit INT DEFAULT 1,
    memory_limit INT DEFAULT 256,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: problem_tags
-- Many-to-many tags for problems
-- ============================================
CREATE TABLE IF NOT EXISTS problem_tags (
    id INT AUTO_INCREMENT PRIMARY KEY,
    problem_id VARCHAR(120) NOT NULL,
    tag VARCHAR(50) NOT NULL,
    
    FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE,
    UNIQUE KEY unique_problem_tag (problem_id, tag),
    INDEX idx_tag (tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: testcases
-- Maps problems to S3 testcase locations
-- ============================================
CREATE TABLE IF NOT EXISTS testcases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    problem_id VARCHAR(120) NOT NULL,
    testcase_number INT NOT NULL,
    s3_input_key VARCHAR(255) NOT NULL,
    s3_output_key VARCHAR(255) NOT NULL,
    is_sample BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE,
    UNIQUE KEY unique_problem_testcase (problem_id, testcase_number),
    INDEX idx_problem_id (problem_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: users
-- User accounts for authentication
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: submissions
-- User submission history
-- ============================================
CREATE TABLE IF NOT EXISTS submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    problem_id VARCHAR(120) NOT NULL,
    language ENUM('python', 'cpp', 'java', 'javascript') NOT NULL,
    code TEXT NOT NULL,
    status ENUM('QUEUED','RUNNING','ACCEPTED','WRONG_ANSWER','TLE','RE','CE') DEFAULT 'QUEUED',
    verdict VARCHAR(50),
    passed_tests INT DEFAULT 0,
    total_tests INT DEFAULT 0,
    execution_time_ms INT,
    memory_used_kb INT,
    error_message TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_problem_id (problem_id),
    INDEX idx_submitted_at (submitted_at),
    INDEX idx_status (status),
    INDEX idx_verdict (verdict)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

