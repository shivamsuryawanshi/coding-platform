# CodeNexus - Online Coding Platform

A complete coding platform like LeetCode/CodeChef with multi-language support, built with modern technologies and automated CI/CD.

## 🎯 Features

- **Multi-language Support**: Python, C++, Java, JavaScript
- **Real-time Code Execution**: Isolated Docker containers for each language
- **Auto-deployment**: GitHub Actions CI/CD pipelines
- **Scalable Architecture**: Microservices-based design

## 🏗️ Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Frontend   │────▶│   Backend   │────▶│   Judges    │
│   (React)   │     │  (Spring)   │     │  (Docker)   │
│   Port:80   │     │  Port:8080  │     │ 5000-5004   │
└─────────────┘     └─────────────┘     └─────────────┘
```

## 📁 Project Structure

```
coding-platform/
├── frontend/          # React + Vite + TypeScript
├── backend/           # Spring Boot REST API
├── judge-python/      # Python 3.10 judge (Port: 5000)
├── judge-cpp/         # GCC C++17 judge (Port: 5002)
├── judge-java/        # JDK 17 judge (Port: 5003)
├── judge-js/          # Node.js LTS judge (Port: 5004)
└── .github/workflows/ # CI/CD pipelines
```

## 🚀 Quick Start

### Local Development

```bash
# Start judges (choose one)
cd judge-python && python judge.py

# Start backend
cd backend && mvn spring-boot:run

# Start frontend
cd frontend && npm install && npm run dev

# Open http://localhost:3000
```

### Docker Deployment

```bash
# Build and run all services
docker build -t judge-python ./judge-python
docker run -d -p 5000:5000 judge-python

docker build -t backend ./backend
docker run -d -p 8080:8080 backend

docker build -t frontend ./frontend
docker run -d -p 80:80 frontend
```

## 🔄 CI/CD

Each service has its own GitHub Actions workflow:

| Service | Trigger Path | Deployment |
|---------|--------------|------------|
| Frontend | `frontend/**` | Auto-deploy to EC2:80 |
| Backend | `backend/**` | Auto-deploy to EC2:8080 |
| Python Judge | `judge-python/**` | Auto-deploy to EC2:5000 |
| C++ Judge | `judge-cpp/**` | Auto-deploy to EC2:5002 |
| Java Judge | `judge-java/**` | Auto-deploy to EC2:5003 |
| JS Judge | `judge-js/**` | Auto-deploy to EC2:5004 |

### Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `EC2_HOST` | EC2 public IP address |
| `EC2_USER` | SSH username (ubuntu) |
| `EC2_SSH_KEY` | SSH private key content |

## 📝 API Endpoints

### Backend API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| GET | `/problem` | Get problem statement |
| GET | `/languages` | Get supported languages |
| POST | `/submit` | Submit code for judging |

### Submit Request

```json
{
  "language": "python",
  "code": "a, b = map(int, input().split())\nprint(a + b)"
}
```

### Submit Response

```json
{
  "verdict": "Accepted",
  "passed": 3,
  "total": 3,
  "failedTest": null
}
```

## 🐳 Docker Images

| Service | Base Image | Size |
|---------|------------|------|
| Frontend | nginx:alpine | ~25MB |
| Backend | eclipse-temurin:17-jre | ~250MB |
| Python Judge | python:3.10-slim | ~150MB |
| C++ Judge | gcc:latest | ~1.2GB |
| Java Judge | eclipse-temurin:17-jdk | ~400MB |
| JS Judge | node:lts-slim | ~200MB |

## ⚙️ Configuration

### Backend (application.yml)

```yaml
judge:
  service:
    host: ${JUDGE_HOST:host.docker.internal}
    ports:
      python: 5000
      cpp: 5002
      java: 5003
      js: 5004
```

### Resource Limits (per container)

- Memory: 512MB (judges), 256MB (frontend)
- CPU: 0.5 cores
- Execution timeout: 5 seconds

## 🔐 Security

- Judges run in isolated containers
- Temp files cleaned after execution
- Hard execution timeouts
- No network access from user code
- Resource limits enforced

## 📊 Verdicts

| Verdict | Description |
|---------|-------------|
| ✅ Accepted | All test cases passed |
| ❌ Wrong Answer | Output doesn't match expected |
| ⚠️ Runtime Error | Code crashed during execution |
| ⏱️ Time Limit Exceeded | Execution took too long |
| 🔨 Compilation Error | Code failed to compile (C++/Java) |

## 📖 Documentation

- [Deployment Guide](DEPLOYMENT.md)
- [Testing Guide](docs/testing.md)

## 🛠️ Tech Stack

- **Frontend**: React, TypeScript, Vite, CSS
- **Backend**: Java 17, Spring Boot 3.2, Maven
- **Judges**: Python, Flask, Gunicorn
- **Infrastructure**: Docker, Nginx, GitHub Actions
- **Cloud**: AWS EC2 (Ubuntu)

## 📄 License

MIT License
