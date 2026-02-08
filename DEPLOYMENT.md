# CodeNexus - Deployment Guide

## Architecture Overview

```
┌────────────────────────────────────────────────────────────────┐
│                         EC2 Instance                           │
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐ │
│  │  Frontend   │───▶│   Backend   │───▶│      Judges         │ │
│  │  (nginx)    │    │ (Spring)    │    │                     │ │
│  │  Port: 80   │    │ Port: 8080  │    │  Python: 5000       │ │
│  └─────────────┘    └─────────────┘    │  C++:    5002       │ │
│                                         │  Java:   5003       │ │
│                                         │  JS:     5004       │ │
│                                         └─────────────────────┘ │
└────────────────────────────────────────────────────────────────┘
```

## Prerequisites

### GitHub Repository Secrets

Configure these secrets in your GitHub repository:

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `EC2_HOST` | EC2 Public IP | `13.60.234.24` |
| `EC2_USER` | SSH Username | `ubuntu` |
| `EC2_SSH_KEY` | Private key content | `-----BEGIN RSA PRIVATE KEY-----...` |

### EC2 Instance Setup

1. **Instance Type**: t3.micro or t3.small
2. **OS**: Ubuntu 22.04 LTS
3. **Security Group Ports**:
   - 22 (SSH)
   - 80 (Frontend)
   - 8080 (Backend API)
   - 5000, 5002, 5003, 5004 (Judges - optional for direct access)

4. **Install Docker on EC2**:
```bash
sudo apt update
sudo apt install -y docker.io
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker ubuntu
# Logout and login again
```

---

## Project Structure

```
coding-platform/
├── frontend/              # React app
│   ├── src/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── backend/               # Spring Boot API
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── judge-python/          # Python judge
│   ├── judge.py
│   ├── testcases.py
│   ├── requirements.txt
│   └── Dockerfile
│
├── judge-cpp/             # C++ judge
│   ├── judge.py
│   ├── testcases.py
│   ├── requirements.txt
│   └── Dockerfile
│
├── judge-java/            # Java judge
│   ├── judge.py
│   ├── testcases.py
│   ├── requirements.txt
│   └── Dockerfile
│
├── judge-js/              # JavaScript judge
│   ├── judge.py
│   ├── testcases.py
│   ├── requirements.txt
│   └── Dockerfile
│
└── .github/workflows/
    ├── frontend.yml       # Frontend CI/CD
    ├── backend.yml        # Backend CI/CD
    ├── judge-python.yml   # Python judge CI/CD
    ├── judge-cpp.yml      # C++ judge CI/CD
    ├── judge-java.yml     # Java judge CI/CD
    ├── judge-js.yml       # JS judge CI/CD
    └── deploy-all.yml     # Deploy everything
```

---

## CI/CD Workflows

### Automatic Triggers

| Workflow | Trigger Path | Action |
|----------|--------------|--------|
| Frontend | `frontend/**` | Build React → Deploy to EC2 |
| Backend | `backend/**` | Build JAR → Deploy to EC2 |
| Python Judge | `judge-python/**` | Build image → Deploy to EC2 |
| C++ Judge | `judge-cpp/**` | Build image → Deploy to EC2 |
| Java Judge | `judge-java/**` | Build image → Deploy to EC2 |
| JS Judge | `judge-js/**` | Build image → Deploy to EC2 |

### Manual Deployment

1. Go to **Actions** tab in GitHub
2. Select **Deploy All Services**
3. Click **Run workflow**

---

## First Time Deployment

### Option 1: GitHub Actions (Recommended)

1. Push code to GitHub
2. Go to Actions → **Deploy All Services**
3. Click **Run workflow**
4. Wait for all jobs to complete

### Option 2: Manual SSH Deployment

```bash
# SSH into EC2
ssh -i your-key.pem ubuntu@EC2_IP

# Pull images (if using Docker Hub) or build locally
# For each judge:
cd /path/to/judge-python
docker build -t judge-python .
docker run -d --name judge-python --restart unless-stopped \
  --memory=512m --cpus=0.5 -p 5000:5000 judge-python

# Repeat for other judges (5002, 5003, 5004)

# Backend
cd /path/to/backend
docker build -t coding-platform-backend .
docker run -d --name backend --restart unless-stopped \
  --memory=512m --cpus=0.5 -p 8080:8080 \
  --add-host=host.docker.internal:host-gateway \
  -e JUDGE_HOST=host.docker.internal \
  coding-platform-backend

# Frontend
cd /path/to/frontend
docker build -t coding-platform-frontend .
docker run -d --name frontend --restart unless-stopped \
  --memory=256m --cpus=0.5 -p 80:80 \
  --add-host=backend:host-gateway \
  coding-platform-frontend
```

---

## Container Management

### Check Running Containers
```bash
docker ps
```

### View Logs
```bash
docker logs -f frontend
docker logs -f backend
docker logs -f judge-python
```

### Restart a Container
```bash
docker restart backend
```

### Stop All Containers
```bash
docker stop frontend backend judge-python judge-cpp judge-java judge-js
```

### Remove All Containers
```bash
docker rm frontend backend judge-python judge-cpp judge-java judge-js
```

---

## Testing the Deployment

### 1. Health Checks

```bash
# On EC2 or from local machine
curl http://EC2_IP:5000/health   # Python Judge
curl http://EC2_IP:5002/health   # C++ Judge
curl http://EC2_IP:5003/health   # Java Judge
curl http://EC2_IP:5004/health   # JS Judge
curl http://EC2_IP:8080/health   # Backend
curl http://EC2_IP               # Frontend
```

### 2. Submit Code via API

```bash
# Python submission
curl -X POST http://EC2_IP:8080/submit \
  -H "Content-Type: application/json" \
  -d '{"language":"python","code":"a, b = map(int, input().split())\nprint(a + b)"}'

# Expected response:
# {"verdict":"Accepted","passed":3,"total":3,"failedTest":null}
```

### 3. Open Frontend

Visit: `http://EC2_IP`

---

## Troubleshooting

### Container Won't Start
```bash
# Check logs
docker logs container-name

# Check if port is already in use
sudo netstat -tlnp | grep PORT
```

### Backend Can't Connect to Judges
```bash
# Verify judges are running
docker ps | grep judge

# Test from backend container
docker exec backend curl http://host.docker.internal:5000/health
```

### Frontend Can't Reach Backend
```bash
# Check nginx logs
docker logs frontend

# Verify backend is accessible
curl http://localhost:8080/health
```

---

## Resource Limits

| Service | Memory | CPU |
|---------|--------|-----|
| Frontend | 256 MB | 0.5 |
| Backend | 512 MB | 0.5 |
| Each Judge | 512 MB | 0.5 |
| **Total** | ~3 GB | ~3.0 |

Recommended EC2: **t3.small** (2 GB RAM) or **t3.medium** (4 GB RAM)

---

## Updating Services

### Update Frontend Only
```bash
# Change files in frontend/
git add frontend/
git commit -m "Update frontend"
git push
# GitHub Actions will auto-deploy
```

### Update a Judge
```bash
# Change files in judge-python/
git add judge-python/
git commit -m "Fix python judge bug"
git push
# Only Python judge will be redeployed
```

---

## Security Notes

1. **Never commit secrets** to the repository
2. **Judges bind to all interfaces** - Consider using internal Docker network for production
3. **No auth** on judge endpoints - Should be behind firewall in production
4. **HTTPS** - Add SSL/TLS using Certbot or AWS ACM for production

---

## URLs

| Service | Local Dev | Production |
|---------|-----------|------------|
| Frontend | http://localhost:3000 | http://EC2_IP |
| Backend | http://localhost:8080 | http://EC2_IP:8080 |
| Python Judge | http://localhost:5000 | http://EC2_IP:5000 |
| C++ Judge | http://localhost:5002 | http://EC2_IP:5002 |
| Java Judge | http://localhost:5003 | http://EC2_IP:5003 |
| JS Judge | http://localhost:5004 | http://EC2_IP:5004 |

