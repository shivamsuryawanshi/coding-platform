# Coding Platform - Core Judgement System

A real-world coding platform architecture with separate Backend (Manager) and Judge (Worker) services.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        CODING PLATFORM                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐          HTTP           ┌─────────────┐       │
│  │             │  POST /judge            │             │       │
│  │   BACKEND   │ ───────────────────────>│    JUDGE    │       │
│  │   (Java)    │                         │  (Python)   │       │
│  │             │ <───────────────────────│             │       │
│  │  Port 8080  │       {verdict}         │  Port 5000  │       │
│  └─────────────┘                         └─────────────┘       │
│        │                                        │               │
│        │                                        │               │
│   - Receives code                          - Executes code     │
│   - Validates input                        - Runs test cases   │
│   - Forwards to judge                      - Compares output   │
│   - Returns verdict                        - Returns verdict   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Why This Design?

### Security
User-submitted code is **untrusted**. It could:
- Contain infinite loops
- Access the filesystem
- Make network requests
- Execute malicious commands

By isolating code execution in a separate service, we can:
- Sandbox the judge (Docker, VM)
- Apply resource limits (CPU, memory, network)
- Kill runaway processes
- Scale independently

### Scalability
```
                     ┌─────────┐
                     │ Judge 1 │
                     └─────────┘
┌─────────┐          ┌─────────┐
│ Backend │────────> │ Judge 2 │
└─────────┘          └─────────┘
                     ┌─────────┐
                     │ Judge 3 │
                     └─────────┘
```

In production:
- Multiple judge instances handle concurrent submissions
- Judges can be deployed on separate EC2 instances
- Each judge runs inside a Docker container
- Load balancer distributes submissions

## Data Flow

```
1. User submits code
   │
   ▼
2. Backend receives POST /submit
   │
   ├── Validates request
   │
   ▼
3. Backend calls Judge POST /judge
   │
   ▼
4. Judge saves code to temp file
   │
   ▼
5. Judge runs code for each test case
   │
   ├── Pass input via STDIN
   ├── Capture STDOUT/STDERR
   ├── Compare with expected output
   │
   ▼
6. Judge returns verdict
   │
   ▼
7. Backend returns verdict to user
```

## Quick Start

### 1. Start the Judge Service

```bash
cd judge

# Install dependencies
pip install -r requirements.txt

# Run the judge
python judge.py
```

Judge will start on http://localhost:5000

### 2. Start the Backend Service

```bash
cd backend

# Build and run (requires Maven and Java 17+)
mvn spring-boot:run
```

Backend will start on http://localhost:8080

### 3. Test the System

#### Correct Solution (Accepted)

**Windows (PowerShell):**
```powershell
$body = @{
    code = "a, b = map(int, input().split())`nprint(a + b)"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/submit" -Method Post -Body $body -ContentType "application/json"
```

**Windows (CMD with curl):**
```cmd
curl -X POST http://localhost:8080/submit -H "Content-Type: application/json" -d "{\"code\": \"a, b = map(int, input().split())\\nprint(a + b)\"}"
```

**Expected Response:**
```json
{
  "verdict": "Accepted",
  "passed": 3,
  "total": 3,
  "failedTest": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### Wrong Answer

```powershell
$body = @{
    code = "a, b = map(int, input().split())`nprint(a - b)"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/submit" -Method Post -Body $body -ContentType "application/json"
```

**Expected Response:**
```json
{
  "verdict": "Wrong Answer",
  "passed": 0,
  "total": 3,
  "failedTest": {
    "test_id": 1,
    "input": "1 2",
    "expected": "3",
    "actual": "-1"
  }
}
```

#### Runtime Error

```powershell
$body = @{
    code = "print(1/0)"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/submit" -Method Post -Body $body -ContentType "application/json"
```

**Expected Response:**
```json
{
  "verdict": "Runtime Error",
  "passed": 0,
  "total": 3,
  "failedTest": {
    "test_id": 1,
    "error": "ZeroDivisionError: division by zero"
  }
}
```

#### Time Limit Exceeded

```powershell
$body = @{
    code = "while True: pass"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/submit" -Method Post -Body $body -ContentType "application/json"
```

**Expected Response:**
```json
{
  "verdict": "Time Limit Exceeded",
  "passed": 0,
  "total": 3,
  "failedTest": {
    "error": "Execution timed out"
  }
}
```

## Project Structure

```
coding-platform/
├── README.md                 # This file
├── backend/                  # Java Spring Boot backend
│   ├── pom.xml
│   ├── README.md
│   └── src/main/
│       ├── java/com/codingplatform/
│       │   ├── CodingPlatformApplication.java
│       │   ├── controller/
│       │   │   └── SubmissionController.java
│       │   ├── service/
│       │   │   └── JudgeService.java
│       │   ├── dto/
│       │   │   ├── SubmissionRequest.java
│       │   │   ├── SubmissionResponse.java
│       │   │   └── JudgeResponse.java
│       │   └── config/
│       │       ├── RestTemplateConfig.java
│       │       └── JudgeServiceConfig.java
│       └── resources/
│           └── application.yml
│
└── judge/                    # Python judge service
    ├── judge.py              # Main judge service
    ├── testcases.py          # Test case definitions
    ├── requirements.txt      # Python dependencies
    └── README.md
```

## Problem Definition

**Problem: Sum of Two Numbers**

Given two integers `a` and `b`, print their sum.

**Input Format:**
Two space-separated integers `a` and `b`

**Output Format:**
A single integer representing `a + b`

**Test Cases:**
| Input | Expected Output |
|-------|-----------------|
| 1 2   | 3               |
| 5 7   | 12              |
| 100 200 | 300           |

**Correct Solution:**
```python
a, b = map(int, input().split())
print(a + b)
```

## Future Roadmap

### Phase 1: Docker Isolation
- Wrap judge in Docker container
- Apply resource limits (CPU, memory)
- Network isolation

### Phase 2: Multi-Language Support
- Add C++ compiler
- Add Java compiler  
- Add JavaScript runtime

### Phase 3: Production Deployment
- Deploy on AWS EC2
- Add load balancer
- Implement queue system (Redis/RabbitMQ)
- Multiple judge instances

### Phase 4: Full Platform
- User authentication
- Problem database
- Submission history
- Leaderboards
- Contests

## Tech Stack

| Component | Technology |
|-----------|------------|
| Backend | Java 17, Spring Boot 3.2 |
| Judge | Python 3.10+, Flask |
| Communication | REST/HTTP JSON |
| Build Tool | Maven |

## License

MIT License

