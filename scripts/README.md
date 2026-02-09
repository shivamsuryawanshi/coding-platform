# Data Migration Scripts

This directory contains scripts for migrating local problem data to AWS (RDS + S3).

## Files

- `schema.sql` - MySQL database schema
- `migrate_questions.py` - Migration script
- `requirements.txt` - Python dependencies

## Prerequisites

1. **AWS Access**
   - On EC2: IAM Role with S3 access attached
   - Locally: AWS CLI configured (`aws configure`)

2. **MySQL Access**
   - RDS endpoint or local MySQL
   - Database created: `coding_platform`

3. **Python 3.10+**

## Setup

```bash
# Install dependencies
pip install -r requirements.txt

# Set environment variables
export DB_HOST=your-rds-endpoint.eu-north-1.rds.amazonaws.com
export DB_PORT=3306
export DB_NAME=coding_platform
export DB_USER=admin
export DB_PASSWORD=your-password
export S3_BUCKET=coding-platform-testcases
export AWS_REGION=eu-north-1
```

## Running Migration

### Step 1: Create Database Schema

```bash
mysql -h $DB_HOST -u $DB_USER -p $DB_NAME < schema.sql
```

### Step 2: Run Migration Script

```bash
python migrate_questions.py
```

The script will:
1. Read all `problem.json` files from `../questions/`
2. Insert problem metadata into RDS `problems` table
3. Insert tags into `problem_tags` table
4. Upload testcases to S3 bucket
5. Insert S3 keys into `testcases` table

## Verification

```sql
-- Check problems
SELECT id, title, category, difficulty FROM problems;

-- Check testcases
SELECT problem_id, testcase_number, s3_input_key FROM testcases;

-- Count by category
SELECT category, COUNT(*) FROM problems GROUP BY category;
```

## S3 Structure

After migration, S3 will contain:

```
coding-platform-testcases/
└── problems/
    ├── sum_of_array_elements/
    │   ├── input1.txt
    │   ├── output1.txt
    │   ├── input2.txt
    │   └── output2.txt
    ├── reverse_linked_list/
    │   ├── input1.txt
    │   └── ...
    └── ...
```

## Troubleshooting

### AWS Access Denied

Ensure your IAM role/user has:
- `s3:PutObject` on the bucket
- `s3:GetObject` on the bucket
- `s3:ListBucket` on the bucket

### MySQL Connection Failed

- Check security group allows your IP
- Verify RDS endpoint is correct
- Confirm database exists

### Missing Testcases

Each problem directory must have:
```
problem.json
testcases/
  input1.txt
  output1.txt
  ...
```

