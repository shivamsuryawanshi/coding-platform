# CodeNexus - Testing Guide

## Architecture Overview

```
┌─────────────┐     ┌─────────────┐     ┌─────────────────────────────────┐
│   Frontend  │────▶│   Backend   │────▶│         EC2 Judges              │
│  React:3000 │     │ Spring:8080 │     │  Python:5000  C++:5002          │
│             │◀────│             │◀────│  Java:5003    JS:5004           │
└─────────────┘     └─────────────┘     └─────────────────────────────────┘
```

## Quick Start

### 1. Start Backend (Spring Boot)
```powershell
cd coding-platform/backend
mvn spring-boot:run
```
Backend runs at: http://localhost:8080

### 2. Start Frontend (React)
```powershell
cd coding-platform/frontend
npm install
npm run dev
```
Frontend runs at: http://localhost:3000

### 3. Open Browser
Navigate to http://localhost:3000

---

## Backend API Testing (curl/PowerShell)

### GET /health
Check status of backend and all judges.

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/health" -Method Get | ConvertTo-Json -Depth 3
```

**Expected Response:**
```json
{
  "status": "healthy",
  "service": "backend",
  "version": "1.0.0",
  "judges": {
    "python": true,
    "cpp": true,
    "java": true,
    "js": true
  }
}
```

### GET /problem
Get the problem statement.

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/problem" -Method Get | ConvertTo-Json -Depth 3
```

### GET /languages
Get list of supported languages.

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/languages" -Method Get | ConvertTo-Json
```

---

## POST /submit - Code Submissions

### Python Submission (Correct)
```powershell
$body = @{
    language = "python"
    code = "a, b = map(int, input().split())`nprint(a + b)"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/submit" -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json
```

**Expected Response:**
```json
{
  "verdict": "Accepted",
  "passed": 3,
  "total": 3,
  "failedTest": null
}
```

### C++ Submission (Correct)
```powershell
$code = @"
#include <iostream>
using namespace std;
int main() {
    int a, b;
    cin >> a >> b;
    cout << a + b;
    return 0;
}
"@

$body = @{ language = "cpp"; code = $code } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/submit" -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json
```

### Java Submission (Correct)
```powershell
$code = @"
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();
        System.out.println(a + b);
    }
}
"@

$body = @{ language = "java"; code = $code } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/submit" -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json
```

### JavaScript Submission (Correct)
```powershell
$code = @"
const readline = require('readline');
const rl = readline.createInterface({ input: process.stdin });
rl.on('line', (line) => {
    const [a, b] = line.split(' ').map(Number);
    console.log(a + b);
    rl.close();
});
"@

$body = @{ language = "js"; code = $code } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/submit" -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json
```

---

## Test Cases for Different Verdicts

### Wrong Answer (Python)
```powershell
$body = @{
    language = "python"
    code = "a, b = map(int, input().split())`nprint(a - b)"  # Wrong: subtracting instead of adding
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/submit" -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json
```

**Expected Response:**
```json
{
  "verdict": "Wrong Answer",
  "passed": 0,
  "total": 3,
  "failedTest": {
    "id": 1,
    "input": "1 2",
    "expected": "3",
    "actual": "-1"
  }
}
```

### Runtime Error (Python)
```powershell
$body = @{
    language = "python"
    code = "x = 1/0"  # Division by zero
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/submit" -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json
```

**Expected Response:**
```json
{
  "verdict": "Runtime Error",
  "passed": 0,
  "total": 3,
  "failedTest": { ... }
}
```

### Compilation Error (C++)
```powershell
$code = @"
#include <iostream>
int main() {
    missing_semicolon
    return 0;
}
"@

$body = @{ language = "cpp"; code = $code } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/submit" -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json
```

**Expected Response:**
```json
{
  "verdict": "Compilation Error",
  "passed": 0,
  "total": 3
}
```

### Time Limit Exceeded (Python)
```powershell
$body = @{
    language = "python"
    code = "while True: pass"  # Infinite loop
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/submit" -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json
```

**Expected Response:**
```json
{
  "verdict": "Time Limit Exceeded",
  "passed": 0,
  "total": 3
}
```

---

## Frontend Testing Checklist

### Basic Functionality
- [ ] Frontend loads at http://localhost:3000
- [ ] Problem statement displays correctly
- [ ] Language dropdown shows all 4 languages
- [ ] Code editor accepts input
- [ ] Submit button is clickable

### Language Switch
- [ ] Switching to Python loads Python starter code
- [ ] Switching to C++ loads C++ starter code
- [ ] Switching to Java loads Java starter code
- [ ] Switching to JavaScript loads JS starter code

### Submission Flow
- [ ] Submit button shows loading state
- [ ] Verdict displays after submission
- [ ] "Accepted" shows green checkmark
- [ ] "Wrong Answer" shows red X and failed test details
- [ ] "Runtime Error" shows warning icon
- [ ] Test case progress dots render correctly

### Edge Cases
- [ ] Empty code shows error message
- [ ] Very long code (max 65536 chars) works
- [ ] Network error shows friendly message

---

## Direct Judge Testing (Bypass Backend)

If you want to test judges directly:

### Python Judge (Port 5000)
```powershell
$body = @{ code = "a, b = map(int, input().split())`nprint(a + b)" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://16.171.153.214:5000/judge" -Method Post -Body $body -ContentType "application/json"
```

### C++ Judge (Port 5002)
```powershell
$code = @"
#include <iostream>
using namespace std;
int main() {
    int a, b;
    cin >> a >> b;
    cout << a + b;
    return 0;
}
"@
$body = @{ code = $code } | ConvertTo-Json
Invoke-RestMethod -Uri "http://16.171.153.214:5002/judge" -Method Post -Body $body -ContentType "application/json"
```

### Java Judge (Port 5003)
```powershell
$code = @"
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();
        System.out.println(a + b);
    }
}
"@
$body = @{ code = $code } | ConvertTo-Json
Invoke-RestMethod -Uri "http://16.171.153.214:5003/judge" -Method Post -Body $body -ContentType "application/json"
```

### JavaScript Judge (Port 5004)
```powershell
$code = @"
const readline = require('readline');
const rl = readline.createInterface({ input: process.stdin });
rl.on('line', (line) => {
    const [a, b] = line.split(' ').map(Number);
    console.log(a + b);
    rl.close();
});
"@
$body = @{ code = $code } | ConvertTo-Json
Invoke-RestMethod -Uri "http://16.171.153.214:5004/judge" -Method Post -Body $body -ContentType "application/json"
```

---

## Configuration

### Switch to Local Judges
Set environment variable before starting backend:
```powershell
$env:JUDGE_HOST = "localhost"
mvn spring-boot:run
```

### Switch to EC2 Judges
```powershell
$env:JUDGE_HOST = "16.171.153.214"
mvn spring-boot:run
```

Or edit `application.yml`:
```yaml
judge:
  service:
    host: 16.171.153.214  # or localhost
```

---

## Troubleshooting

### Backend can't connect to judges
1. Check if EC2 instance is running
2. Check security group allows ports 5000, 5002, 5003, 5004
3. Check Docker containers are running: `docker ps`

### Frontend shows "Unable to load problem"
1. Ensure backend is running on port 8080
2. Check browser console for CORS errors
3. Verify Vite proxy is configured in `vite.config.ts`

### Submission times out
1. Judges have 5-second timeout per test case
2. Check if code has infinite loops
3. Check EC2 instance health

---

## EC2 Judge URLs

| Language   | Port | URL                                |
|------------|------|------------------------------------|
| Python     | 5000 | http://16.171.153.214:5000/judge   |
| C++        | 5002 | http://16.171.153.214:5002/judge   |
| Java       | 5003 | http://16.171.153.214:5003/judge   |
| JavaScript | 5004 | http://16.171.153.214:5004/judge   |

