# 📦 Data Migration Guide
## RDS (Questions) + S3 (Testcases) Migration

---

## 🎯 Overview

Yeh migration script **local questions folder** se:
1. **Problem metadata** → **AWS RDS MySQL** (`problems` table)
2. **Tags** → **AWS RDS MySQL** (`problem_tags` table)
3. **Testcase files** → **AWS S3** (`coding-platform-testcases` bucket)
4. **S3 keys** → **AWS RDS MySQL** (`testcases` table)

---

## 📁 Input Structure

```
questions/
├── arrays/
│   ├── easy/
│   │   ├── check_array_sorted/
│   │   │   ├── problem.json          ← Problem metadata
│   │   │   └── testcases/
│   │   │       ├── input1.txt        ← Testcase input
│   │   │       ├── output1.txt       ← Expected output
│   │   │       ├── input2.txt
│   │   │       └── output2.txt
│   │   └── sum_of_array_elements/
│   │       └── ...
```

---

## 🔄 Migration Flow

### Step 1: Find All Problems
```python
# Script finds all problem.json files recursively
questions/**/problem.json
```

### Step 2: For Each Problem

#### 2.1: Parse problem.json
```json
{
  "id": "check_array_sorted",
  "title": "Check If Array Is Sorted",
  "category": "arrays",
  "difficulty": "easy",
  "statement": "...",
  "tags": ["arrays", "basic"]
}
```

#### 2.2: Insert into RDS - `problems` table
```sql
INSERT INTO problems (id, title, category, difficulty, statement, ...)
VALUES ('check_array_sorted', 'Check If Array Is Sorted', 'arrays', 'easy', ...)
```

#### 2.3: Insert Tags into RDS - `problem_tags` table
```sql
INSERT INTO problem_tags (problem_id, tag)
VALUES ('check_array_sorted', 'arrays'),
       ('check_array_sorted', 'basic')
```

#### 2.4: Upload Testcases to S3
```python
# For each testcase pair:
s3.upload_file('input1.txt', 'problems/check_array_sorted/input1.txt')
s3.upload_file('output1.txt', 'problems/check_array_sorted/output1.txt')
```

**S3 Structure:**
```
coding-platform-testcases/
└── problems/
    ├── check_array_sorted/
    │   ├── input1.txt    ✅ Uploaded
    │   ├── output1.txt   ✅ Uploaded
    │   ├── input2.txt    ✅ Uploaded
    │   └── output2.txt   ✅ Uploaded
```

#### 2.5: Store S3 Keys in RDS - `testcases` table
```sql
INSERT INTO testcases (problem_id, testcase_number, s3_input_key, s3_output_key, is_sample)
VALUES (
  'check_array_sorted',
  1,
  'problems/check_array_sorted/input1.txt',
  'problems/check_array_sorted/output1.txt',
  TRUE  -- First 2 are samples
)
```

---

## 📊 Final State

### RDS MySQL Database:
```sql
-- problems table
SELECT * FROM problems;
+---------------------+------------------------+----------+----------+
| id                  | title                  | category | difficulty|
+---------------------+------------------------+----------+----------+
| check_array_sorted  | Check If Array Is Sorted| arrays  | easy     |
| sum_of_array_elements| Sum of Array Elements | arrays   | easy     |
| ...                 | ...                    | ...      | ...      |
+---------------------+------------------------+----------+----------+

-- testcases table (S3 references)
SELECT * FROM testcases WHERE problem_id = 'check_array_sorted';
+----+---------------------+------------------+----------------------------------------+-----------------------------------------+
| id | problem_id          | testcase_number  | s3_input_key                            | s3_output_key                            |
+----+---------------------+------------------+----------------------------------------+-----------------------------------------+
| 1  | check_array_sorted  | 1                | problems/check_array_sorted/input1.txt  | problems/check_array_sorted/output1.txt  |
| 2  | check_array_sorted  | 2                | problems/check_array_sorted/input2.txt  | problems/check_array_sorted/output2.txt  |
+----+---------------------+------------------+----------------------------------------+-----------------------------------------+
```

### AWS S3 Bucket:
```
coding-platform-testcases/
└── problems/
    ├── check_array_sorted/
    │   ├── input1.txt    (Actual file content)
    │   ├── output1.txt   (Actual file content)
    │   ├── input2.txt
    │   └── output2.txt
    ├── sum_of_array_elements/
    │   └── ...
    └── ... (all problems)
```

---

## 🔐 Security & Authentication

### RDS Connection:
- **Environment Variables:**
  - `DB_HOST` - RDS endpoint
  - `DB_USER` - Database username
  - `DB_PASSWORD` - Database password
  - `DB_NAME` - Database name
  - `DB_PORT` - Port (3306)

### S3 Upload:
- **IAM Role Authentication** (No AWS keys needed!)
- EC2 instance pe attached IAM Role: `coding-platform-ec2-role`
- Policy: `AmazonS3ReadOnlyAccess` (read) + `PutObject` (write)
- **Environment Variables:**
  - `S3_BUCKET` - Bucket name (`coding-platform-testcases`)
  - `AWS_REGION` - Region (`eu-north-1`)

---

## 🚀 Running Migration

### Via CI/CD (Recommended):
```yaml
# GitHub Actions workflow automatically runs:
1. Copy scripts/ and questions/ to EC2
2. Install dependencies (boto3, mysql-connector-python)
3. Run schema.sql
4. Run migrate_questions.py
5. Verify migration
```

### Manual Run (EC2):
```bash
# Set environment variables
export DB_HOST=coding-platform-db.c384ky4cslv5.eu-north-1.rds.amazonaws.com
export DB_USER=admin
export DB_PASSWORD=shivam123shivam
export DB_NAME=coding_platform
export DB_PORT=3306
export S3_BUCKET=coding-platform-testcases
export AWS_REGION=eu-north-1

# Run migration
cd ~/codenexus/scripts
python3 migrate_questions.py
```

---

## ✅ Verification

### Check RDS:
```sql
-- Count problems
SELECT COUNT(*) FROM problems;

-- Count testcases
SELECT COUNT(*) FROM testcases;

-- Sample data
SELECT p.id, p.title, COUNT(t.id) as testcase_count
FROM problems p
LEFT JOIN testcases t ON p.id = t.problem_id
GROUP BY p.id
LIMIT 10;
```

### Check S3:
```python
import boto3

s3 = boto3.client('s3', region_name='eu-north-1')
response = s3.list_objects_v2(
    Bucket='coding-platform-testcases',
    Prefix='problems/'
)

print(f"Total files: {len(response.get('Contents', []))}")
```

---

## 📝 Migration Script Features

1. **Idempotent**: Safe to run multiple times (uses `ON DUPLICATE KEY UPDATE`)
2. **Transaction-safe**: Each problem migrated in a transaction
3. **Error handling**: Logs errors, continues with next problem
4. **Progress logging**: Shows which problem is being migrated
5. **S3 verification**: Checks bucket access before starting
6. **Database verification**: Shows counts after migration

---

## 🎯 Summary

| Component | Destination | What Gets Stored |
|-----------|-------------|------------------|
| Problem Metadata | **RDS** (`problems` table) | Title, difficulty, statement, constraints |
| Tags | **RDS** (`problem_tags` table) | Problem tags (arrays, basic, etc.) |
| Testcase Files | **S3** (`problems/{id}/inputX.txt`) | Actual testcase file content |
| S3 References | **RDS** (`testcases` table) | S3 keys pointing to testcase files |

**Key Point:** Testcase **files** S3 mein hain, lekin **references** (S3 keys) RDS mein store hain taaki backend easily fetch kar sake!

---

## 🔗 Related Files

- `scripts/migrate_questions.py` - Main migration script
- `scripts/schema.sql` - Database schema
- `.github/workflows/deploy-all.yml` - CI/CD workflow
- `questions/**/problem.json` - Problem metadata files
- `questions/**/testcases/*.txt` - Testcase files

---

**Migration Complete! ✅**

