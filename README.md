# CodeNexus - Production-Ready Online Coding Platform

A complete LeetCode-style coding platform built with **Java Spring Boot**, **React**, and **AWS** (RDS + S3).

## 🎯 Features

- ✅ **Multi-language Support**: Python, C++, Java, JavaScript
- ✅ **100+ DSA Problems**: Arrays, Trees, Graphs, DP, and more
- ✅ **Real-time Code Execution**: Isolated execution environment
- ✅ **AWS Integration**: RDS (MySQL) + S3 for production
- ✅ **IAM Role Authentication**: No AWS keys in code
- ✅ **Clean Architecture**: Controller → Service → Repository pattern
- ✅ **CI/CD Ready**: Docker containers for all services

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         AWS Cloud                                │
│                                                                  │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐  │
│  │ Frontend │───▶│ Backend  │───▶│  Judge   │    │   RDS    │  │
│  │ (React)  │    │ (Spring) │    │ (Python) │    │ (MySQL)  │  │
│  │ Port: 80 │    │Port: 8080│    │Port: 5000│    │Port: 3306│  │
│  └──────────┘    └────┬─────┘    └────┬─────┘    └──────────┘  │
│                       │               │                         │
│                       │               │         ┌──────────┐   │
│                       └───────────────┼────────▶│    S3    │   │
│                                       └────────▶│ Testcases│   │
│                                                 └──────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### Data Flow

1. **Frontend** → Displays problems, accepts code submissions
2. **Backend** → Fetches problems from RDS, fetches testcases from S3
3. **Backend** → Sends code + testcases to Judge
4. **Judge** → Executes code, returns verdict
5. **Backend** → Returns result to Frontend

---

## 📁 Project Structure

```
coding-platform/
├── backend/                 # Java Spring Boot API
│   ├── src/main/java/
│   │   └── com/codingplatform/
│   │       ├── controller/  # REST endpoints
│   │       ├── service/     # Business logic
│   │       ├── repository/  # JPA repositories
│   │       ├── entity/      # Database entities
│   │       ├── dto/         # Data transfer objects
│   │       ├── config/      # AWS, Judge configs
│   │       └── exception/   # Error handlers
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/               # React + Tailwind CSS
│   ├── src/
│   │   ├── components/     # Reusable UI components
│   │   ├── pages/          # Page components
│   │   ├── api.ts          # API client
│   │   └── types.ts        # TypeScript types
│   ├── Dockerfile
│   └── package.json
│
├── judge/                  # Unified Python Judge
│   ├── judge.py            # Multi-language executor
│   ├── Dockerfile
│   └── requirements.txt
│
├── scripts/                # Data migration tools
│   ├── schema.sql          # MySQL schema
│   ├── migrate_questions.py
│   └── requirements.txt
│
└── questions/              # Problem bank (100+ problems)
    ├── arrays/
    ├── trees/
    ├── graphs/
    └── ...
```

---

## 🔧 AWS Configuration

### Resources Required

| Resource | Name | Details |
|----------|------|---------|
| **EC2** | coding-platform-ec2 | t3.medium, Ubuntu 22.04 |
| **RDS** | coding-platform-db | MySQL 8.0, db.t3.micro |
| **S3** | coding-platform-testcases | Private bucket |
| **IAM Role** | coding-platform-ec2-role | S3ReadOnlyAccess |

### S3 Structure

```
coding-platform-testcases/
└── problems/
    └── <problem_id>/
        ├── input1.txt
        ├── output1.txt
        ├── input2.txt
        └── output2.txt
```

### Security

- ✅ **IAM Role** for S3 access (no access keys)
- ✅ **Environment variables** for database credentials
- ✅ **No secrets in code**

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Python 3.10+
- Docker (optional)
- MySQL 8 (local) or AWS RDS

### 1. Database Setup

```bash
# Connect to MySQL and create database
mysql -u root -p
CREATE DATABASE coding_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Run schema
mysql -u root -p coding_platform < scripts/schema.sql
```

### 2. Data Migration

```bash
cd scripts

# Install dependencies
pip install -r requirements.txt

# Set environment variables
export DB_HOST=localhost
export DB_USER=root
export DB_PASSWORD=your_password
export DB_NAME=coding_platform
export S3_BUCKET=coding-platform-testcases
export AWS_REGION=eu-north-1

# Run migration (uploads to S3 + inserts to RDS)
python migrate_questions.py
```

### 3. Start Backend

```bash
cd backend

# Set environment variables
export DB_HOST=localhost
export DB_USER=root
export DB_PASSWORD=your_password
export DB_NAME=coding_platform
export AWS_REGION=eu-north-1
export S3_BUCKET=coding-platform-testcases
export JUDGE_HOST=localhost
export JUDGE_PORT=5000

# Run
mvn spring-boot:run
```

### 4. Start Judge

```bash
cd judge

# Install dependencies
pip install -r requirements.txt

# Run
python judge.py
```

### 5. Start Frontend

```bash
cd frontend

# Install dependencies
npm install

# Run (proxies API to localhost:8080)
npm run dev
```

### 6. Open Browser

Visit: http://localhost:3000

---

## 🐳 Docker Deployment

### Build Images

```bash
# Backend
docker build -t codenexus-backend ./backend

# Judge
docker build -t codenexus-judge ./judge

# Frontend
docker build -t codenexus-frontend ./frontend
```

### Run Containers

```bash
# Judge
docker run -d --name judge \
  -p 5000:5000 \
  codenexus-judge

# Backend
docker run -d --name backend \
  -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_USER=admin \
  -e DB_PASSWORD=your_password \
  -e DB_NAME=coding_platform \
  -e AWS_REGION=eu-north-1 \
  -e S3_BUCKET=coding-platform-testcases \
  -e JUDGE_HOST=host.docker.internal \
  -e JUDGE_PORT=5000 \
  codenexus-backend

# Frontend
docker run -d --name frontend \
  -p 80:80 \
  codenexus-frontend
```

---

## 📝 API Reference

### Problems

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/problems` | List all problems |
| GET | `/api/problems?category=arrays` | Filter by category |
| GET | `/api/problems?difficulty=easy` | Filter by difficulty |
| GET | `/api/problems/{id}` | Get problem details |
| GET | `/api/categories` | List all categories |
| GET | `/api/stats` | Get problem statistics |

### Submissions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/submit` | Submit code |
| GET | `/api/languages` | Supported languages |
| GET | `/api/health` | Health check |

### Submit Request

```json
{
  "problemId": "sum_of_array_elements",
  "language": "python",
  "code": "n = int(input())\narr = list(map(int, input().split()))\nprint(sum(arr))"
}
```

### Submit Response

```json
{
  "submissionId": 123,
  "problemId": "sum_of_array_elements",
  "language": "python",
  "verdict": "Accepted",
  "passed": 5,
  "total": 5,
  "failedTest": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 📊 Verdicts

| Verdict | Description |
|---------|-------------|
| ✅ Accepted | All test cases passed |
| ❌ Wrong Answer | Output mismatch |
| ⚠️ Runtime Error | Code crashed |
| ⏱️ Time Limit Exceeded | Execution timeout |
| 🔨 Compilation Error | Code failed to compile |

---

## 🛡️ Security Best Practices

1. **IAM Role for S3** - EC2 uses instance role, no access keys
2. **Environment Variables** - Database credentials via ENV
3. **No Hardcoded Secrets** - All secrets externalized
4. **Isolated Execution** - Judge runs code in temp directories
5. **Resource Limits** - Timeout and memory limits enforced

---

## 📈 Production Checklist

- [ ] Set up RDS with proper security groups
- [ ] Create S3 bucket with IAM policy
- [ ] Attach IAM role to EC2
- [ ] Run data migration script
- [ ] Configure environment variables
- [ ] Set up SSL/TLS (use AWS ACM)
- [ ] Configure CloudWatch for monitoring
- [ ] Set up GitHub Actions for CI/CD

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18, TypeScript, Tailwind CSS, Vite |
| Backend | Java 17, Spring Boot 3.2, Spring Data JPA |
| Judge | Python 3.10, Flask, Gunicorn |
| Database | MySQL 8 (AWS RDS) |
| Storage | AWS S3 |
| Auth | AWS IAM Roles |
| Container | Docker, nginx |

---

## 📄 License

MIT License
