#!/usr/bin/env python3
"""
CodeNexus Data Migration Script
================================
Migrates local question data to AWS RDS (MySQL) + S3

This script:
1. Reads all problem.json files from questions/ directory
2. Inserts problem metadata into RDS MySQL
3. Uploads testcases to S3
4. Stores S3 keys in testcases table

Requirements:
- boto3 (uses IAM Role - no credentials needed on EC2)
- mysql-connector-python
- Environment variables for DB connection

Usage:
    # Set environment variables
    export DB_HOST=your-rds-endpoint.eu-north-1.rds.amazonaws.com
    export DB_USER=admin
    export DB_PASSWORD=your-password
    export DB_NAME=coding_platform
    export DB_PORT=3306
    export S3_BUCKET=coding-platform-testcases
    export AWS_REGION=eu-north-1

    # Run migration
    python migrate_questions.py
"""

import os
import json
import glob
import logging
import mysql.connector
from mysql.connector import Error
import boto3
from botocore.exceptions import ClientError
from pathlib import Path

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# ============================================
# Configuration from Environment Variables
# ============================================
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'user': os.getenv('DB_USER', 'admin'),
    'password': os.getenv('DB_PASSWORD', ''),
    'database': os.getenv('DB_NAME', 'coding_platform'),
    'port': int(os.getenv('DB_PORT', 3306)),
    'charset': 'utf8mb4',
    'collation': 'utf8mb4_unicode_ci'
}

S3_BUCKET = os.getenv('S3_BUCKET', 'coding-platform-testcases')
AWS_REGION = os.getenv('AWS_REGION', 'eu-north-1')

# Questions directory (relative to script location)
SCRIPT_DIR = Path(__file__).parent
QUESTIONS_DIR = SCRIPT_DIR.parent / 'questions'


class DatabaseManager:
    """Manages MySQL database connections and operations."""
    
    def __init__(self):
        self.connection = None
        self.cursor = None
    
    def connect(self):
        """Establish database connection."""
        try:
            self.connection = mysql.connector.connect(**DB_CONFIG)
            self.cursor = self.connection.cursor(dictionary=True)
            logger.info(f"Connected to MySQL: {DB_CONFIG['host']}/{DB_CONFIG['database']}")
            return True
        except Error as e:
            logger.error(f"Database connection failed: {e}")
            return False
    
    def disconnect(self):
        """Close database connection."""
        if self.cursor:
            self.cursor.close()
        if self.connection:
            self.connection.close()
            logger.info("Database connection closed")
    
    def execute(self, query, params=None):
        """Execute a query with optional parameters."""
        try:
            self.cursor.execute(query, params or ())
            return True
        except Error as e:
            logger.error(f"Query failed: {e}\nQuery: {query}")
            return False
    
    def commit(self):
        """Commit the current transaction."""
        self.connection.commit()
    
    def rollback(self):
        """Rollback the current transaction."""
        self.connection.rollback()


class S3Manager:
    """Manages S3 operations using IAM Role authentication."""
    
    def __init__(self):
        # boto3 automatically uses IAM Role on EC2
        # For local testing, configure AWS CLI or set credentials
        self.s3_client = boto3.client('s3', region_name=AWS_REGION)
        self.bucket = S3_BUCKET
    
    def upload_file(self, local_path: str, s3_key: str) -> bool:
        """Upload a file to S3."""
        try:
            with open(local_path, 'rb') as f:
                self.s3_client.put_object(
                    Bucket=self.bucket,
                    Key=s3_key,
                    Body=f.read(),
                    ContentType='text/plain'
                )
            logger.info(f"  ✓ Uploaded to S3: {s3_key}")
            return True
        except ClientError as e:
            logger.error(f"S3 upload failed for {s3_key}: {e}")
            return False
        except FileNotFoundError:
            logger.error(f"Local file not found: {local_path}")
            return False
    
    def check_bucket_access(self) -> bool:
        """Verify we have access to the S3 bucket."""
        try:
            self.s3_client.head_bucket(Bucket=self.bucket)
            logger.info(f"S3 bucket accessible: {self.bucket}")
            return True
        except ClientError as e:
            error_code = e.response.get('Error', {}).get('Code', '')
            if error_code == '403':
                logger.error(f"Access denied to bucket: {self.bucket}")
            elif error_code == '404':
                logger.error(f"Bucket does not exist: {self.bucket}")
            else:
                logger.error(f"S3 bucket check failed: {e}")
            return False


def find_all_problems() -> list:
    """Find all problem.json files in the questions directory."""
    problems = []
    pattern = str(QUESTIONS_DIR / '**' / 'problem.json')
    
    for problem_file in glob.glob(pattern, recursive=True):
        problem_path = Path(problem_file)
        problem_dir = problem_path.parent
        testcases_dir = problem_dir / 'testcases'
        
        problems.append({
            'json_path': problem_path,
            'problem_dir': problem_dir,
            'testcases_dir': testcases_dir
        })
    
    logger.info(f"Found {len(problems)} problems")
    return problems


def parse_problem_json(json_path: Path) -> dict:
    """Parse a problem.json file."""
    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except json.JSONDecodeError as e:
        logger.error(f"Invalid JSON in {json_path}: {e}")
        return None
    except Exception as e:
        logger.error(f"Error reading {json_path}: {e}")
        return None


def find_testcases(testcases_dir: Path) -> list:
    """Find all input/output testcase pairs."""
    testcases = []
    
    if not testcases_dir.exists():
        logger.warning(f"No testcases directory: {testcases_dir}")
        return testcases
    
    # Find all input files and match with output files
    input_files = sorted(testcases_dir.glob('input*.txt'))
    
    for input_file in input_files:
        # Extract testcase number from filename (e.g., input1.txt -> 1)
        num = ''.join(filter(str.isdigit, input_file.stem))
        if not num:
            continue
        
        output_file = testcases_dir / f'output{num}.txt'
        
        if output_file.exists():
            testcases.append({
                'number': int(num),
                'input_path': input_file,
                'output_path': output_file
            })
        else:
            logger.warning(f"Missing output file for: {input_file}")
    
    return sorted(testcases, key=lambda x: x['number'])


def insert_problem(db: DatabaseManager, problem_data: dict) -> bool:
    """Insert or update a problem in the database."""
    query = """
        INSERT INTO problems (id, title, category, difficulty, statement, 
                             input_format, output_format, constraints, 
                             time_limit, memory_limit)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE
            title = VALUES(title),
            category = VALUES(category),
            difficulty = VALUES(difficulty),
            statement = VALUES(statement),
            input_format = VALUES(input_format),
            output_format = VALUES(output_format),
            constraints = VALUES(constraints),
            time_limit = VALUES(time_limit),
            memory_limit = VALUES(memory_limit),
            updated_at = CURRENT_TIMESTAMP
    """
    
    # Convert constraints list to string if needed
    constraints = problem_data.get('constraints', '')
    if isinstance(constraints, list):
        constraints = '\n'.join(constraints)
    
    params = (
        problem_data['id'],
        problem_data.get('title', ''),
        problem_data.get('category', 'misc'),
        problem_data.get('difficulty', 'easy').lower(),
        problem_data.get('statement', ''),
        problem_data.get('input_format', ''),
        problem_data.get('output_format', ''),
        constraints,
        problem_data.get('time_limit', 1),
        problem_data.get('memory_limit', 256)
    )
    
    return db.execute(query, params)


def insert_tags(db: DatabaseManager, problem_id: str, tags: list) -> bool:
    """Insert tags for a problem."""
    # Delete existing tags first
    db.execute("DELETE FROM problem_tags WHERE problem_id = %s", (problem_id,))
    
    # Insert new tags
    for tag in tags:
        query = """
            INSERT INTO problem_tags (problem_id, tag)
            VALUES (%s, %s)
            ON DUPLICATE KEY UPDATE tag = tag
        """
        if not db.execute(query, (problem_id, tag)):
            return False
    
    return True


def insert_testcase(db: DatabaseManager, problem_id: str, testcase_num: int,
                    s3_input_key: str, s3_output_key: str, is_sample: bool = False) -> bool:
    """Insert a testcase record pointing to S3."""
    query = """
        INSERT INTO testcases (problem_id, testcase_number, s3_input_key, 
                               s3_output_key, is_sample)
        VALUES (%s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE
            s3_input_key = VALUES(s3_input_key),
            s3_output_key = VALUES(s3_output_key),
            is_sample = VALUES(is_sample)
    """
    
    params = (problem_id, testcase_num, s3_input_key, s3_output_key, is_sample)
    return db.execute(query, params)


def migrate_problem(db: DatabaseManager, s3: S3Manager, problem_info: dict) -> bool:
    """Migrate a single problem to RDS + S3."""
    # Parse problem.json
    problem_data = parse_problem_json(problem_info['json_path'])
    if not problem_data:
        return False
    
    problem_id = problem_data.get('id')
    if not problem_id:
        logger.error(f"Problem has no ID: {problem_info['json_path']}")
        return False
    
    logger.info(f"Migrating: {problem_id}")
    
    # Insert problem metadata
    if not insert_problem(db, problem_data):
        return False
    
    # Insert tags
    tags = problem_data.get('tags', [])
    if tags and not insert_tags(db, problem_id, tags):
        return False
    
    # Find and upload testcases
    testcases = find_testcases(problem_info['testcases_dir'])
    
    for tc in testcases:
        # Generate S3 keys
        s3_input_key = f"problems/{problem_id}/input{tc['number']}.txt"
        s3_output_key = f"problems/{problem_id}/output{tc['number']}.txt"
        
        # Upload to S3
        if not s3.upload_file(str(tc['input_path']), s3_input_key):
            return False
        if not s3.upload_file(str(tc['output_path']), s3_output_key):
            return False
        
        # Mark first 2 testcases as samples
        is_sample = tc['number'] <= 2
        
        # Insert testcase record
        if not insert_testcase(db, problem_id, tc['number'], 
                               s3_input_key, s3_output_key, is_sample):
            return False
    
    logger.info(f"  ✓ {len(testcases)} testcases uploaded")
    return True


def main():
    """Main migration function."""
    logger.info("=" * 60)
    logger.info("CodeNexus Data Migration")
    logger.info("=" * 60)
    
    # Validate questions directory
    if not QUESTIONS_DIR.exists():
        logger.error(f"Questions directory not found: {QUESTIONS_DIR}")
        return False
    
    # Initialize managers
    db = DatabaseManager()
    s3 = S3Manager()
    
    # Check connections
    if not db.connect():
        return False
    
    if not s3.check_bucket_access():
        db.disconnect()
        return False
    
    try:
        # Find all problems
        problems = find_all_problems()
        
        if not problems:
            logger.warning("No problems found to migrate")
            return True
        
        # Migrate each problem
        success_count = 0
        fail_count = 0
        
        for problem_info in problems:
            try:
                if migrate_problem(db, s3, problem_info):
                    success_count += 1
                    db.commit()
                else:
                    fail_count += 1
                    db.rollback()
            except Exception as e:
                logger.error(f"Error migrating problem: {e}")
                fail_count += 1
                db.rollback()
        
        # Summary
        logger.info("=" * 60)
        logger.info("Migration Complete")
        logger.info(f"  ✓ Successful: {success_count}")
        logger.info(f"  ✗ Failed: {fail_count}")
        logger.info("=" * 60)
        
        return fail_count == 0
        
    finally:
        db.disconnect()


if __name__ == '__main__':
    import sys
    success = main()
    sys.exit(0 if success else 1)

