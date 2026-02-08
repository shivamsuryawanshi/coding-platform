# Judge Service

## Overview

The Judge Service is the **code execution engine** of the coding platform. It is responsible for:

1. Receiving user-submitted code via HTTP
2. Saving code to a temporary file
3. Executing the code using subprocess
4. Running test cases and capturing output
5. Comparing output against expected results
6. Returning a verdict (Accepted, Wrong Answer, Runtime Error, Time Limit Exceeded)

## Why is the Judge Separate?

**Security**: User-submitted code is untrusted. It could:
- Contain infinite loops
- Attempt to access the filesystem
- Try to make network requests
- Consume excessive memory
- Execute malicious commands

By isolating the judge:
- The main backend remains secure
- The judge can be sandboxed (Docker, VM, etc.)
- The judge can be scaled independently
- Resource limits can be enforced at the container/VM level

## Architecture

```
┌─────────────────┐         ┌─────────────────┐
│                 │  HTTP   │                 │
│  Java Backend   │────────>│  Python Judge   │
│  (Manager)      │         │  (Worker)       │
│                 │<────────│                 │
└─────────────────┘         └─────────────────┘
     Port 8080                   Port 5000
```

## API Reference

### POST /judge

Submit code for evaluation.

**Request:**
```json
{
    "code": "a, b = map(int, input().split())\nprint(a + b)"
}
```

**Response (Success):**
```json
{
    "verdict": "Accepted",
    "passed": 3,
    "total": 3,
    "failed_test": null
}
```

**Response (Wrong Answer):**
```json
{
    "verdict": "Wrong Answer",
    "passed": 1,
    "total": 3,
    "failed_test": {
        "test_id": 2,
        "input": "5 7",
        "expected": "12",
        "actual": "57",
        "error": null
    }
}
```

### GET /health

Health check endpoint.

**Response:**
```json
{
    "status": "healthy",
    "service": "judge",
    "version": "1.0.0"
}
```

## Running the Service

```bash
# Install dependencies
pip install -r requirements.txt

# Run the judge
python judge.py
```

The service will start on `http://localhost:5000`.

## Testing

```bash
# Test with correct solution
curl -X POST http://localhost:5000/judge ^
  -H "Content-Type: application/json" ^
  -d "{\"code\": \"a, b = map(int, input().split())\nprint(a + b)\"}"

# Expected: {"verdict": "Accepted", ...}
```

## Future Enhancements

1. **Docker Isolation**: Wrap execution in a container
2. **Multi-language Support**: Add C++, Java, JavaScript compilers
3. **Resource Limits**: CPU time, memory limits via cgroups
4. **Parallel Execution**: Run test cases concurrently
5. **Custom Test Cases**: Support problem-specific test data

