# Coding Platform Backend

## Overview

The Backend Service is the **MANAGER** component of the coding platform. It:

1. Exposes REST APIs for clients (web, mobile, CLI)
2. Validates incoming requests
3. Forwards code to the Judge Service
4. Returns verdicts to clients

## Why is the Backend Separate from the Judge?

**The backend NEVER executes user code directly.**

This separation is fundamental to the security and scalability of the platform:

| Concern | Backend | Judge |
|---------|---------|-------|
| User authentication | ✓ | ✗ |
| Database access | ✓ | ✗ |
| Code execution | ✗ | ✓ |
| Scaling | Horizontal | Horizontal |
| Security | Standard | Sandboxed |

## Architecture

```
┌──────────┐      ┌─────────────┐      ┌─────────────┐
│  Client  │─────>│   Backend   │─────>│    Judge    │
│          │<─────│  (Manager)  │<─────│  (Worker)   │
└──────────┘      └─────────────┘      └─────────────┘
                    Port 8080            Port 5000
```

## API Reference

### POST /submit

Submit code for evaluation.

**Request:**
```json
{
    "code": "a, b = map(int, input().split())\nprint(a + b)"
}
```

**Response:**
```json
{
    "verdict": "Accepted",
    "passed": 3,
    "total": 3,
    "failedTest": null,
    "timestamp": "2024-01-15T10:30:00Z"
}
```

### GET /health

Check backend and judge health.

**Response:**
```json
{
    "status": "healthy",
    "service": "backend",
    "version": "1.0.0",
    "judge": {
        "status": "healthy"
    }
}
```

## Running the Backend

### Prerequisites
- Java 17+
- Maven 3.6+
- Judge service running on port 5000

### Build and Run

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# Or run the JAR directly
java -jar target/coding-platform-backend-1.0.0.jar
```

The backend will start on `http://localhost:8080`.

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
judge:
  service:
    url: http://localhost:5000  # Change for different environments
```

## Data Flow

1. Client sends `POST /submit` with code
2. Backend validates the request
3. Backend forwards code to `POST /judge` on the Judge Service
4. Judge executes code, runs test cases
5. Judge returns verdict to Backend
6. Backend returns verdict to Client

## Future Enhancements

1. **Database Integration**: Store submissions, users, problems
2. **Authentication**: JWT-based auth
3. **Rate Limiting**: Prevent abuse
4. **Queue System**: Redis/RabbitMQ for async processing
5. **Multiple Judges**: Load balancing across judge instances

