# AWS EC2 Deployment Guide - Judge Service

This document explains how to deploy the Dockerized Judge Service to AWS EC2.

---

## 1. EC2 Instance Selection

### Recommended Instance Type: `t3.small` or `t3.micro`

| Aspect | Recommendation | Reason |
|--------|----------------|--------|
| **Instance Type** | `t3.small` | 2 vCPU, 2GB RAM - sufficient for Python code execution |
| **Alternative** | `t3.micro` | 1 vCPU, 1GB RAM - for testing/low traffic (Free Tier eligible) |
| **For Production** | `t3.medium` | 2 vCPU, 4GB RAM - if handling concurrent submissions |

**Why t3 family?**
- Burstable performance (good for variable workloads)
- Cost-effective for code execution tasks
- Sufficient CPU for Python subprocess execution
- Network performance adequate for API calls

### Operating System: Amazon Linux 2023 or Ubuntu 22.04 LTS

| OS | AMI | Reason |
|----|-----|--------|
| **Amazon Linux 2023** | `al2023-ami-*` | Optimized for AWS, easy Docker install |
| **Ubuntu 22.04 LTS** | `ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-*` | Familiar, well-documented, LTS support |

**Recommendation:** Amazon Linux 2023 (better AWS integration)

---

## 2. Security Group Configuration

### Required Inbound Rules:

| Type | Protocol | Port | Source | Purpose |
|------|----------|------|--------|---------|
| SSH | TCP | 22 | Your IP (e.g., `203.0.113.0/32`) | Remote access for setup |
| Custom TCP | TCP | 5000 | Backend IP or `0.0.0.0/0` | Judge API access |
| HTTP | TCP | 80 | `0.0.0.0/0` | (Optional) If using reverse proxy |
| HTTPS | TCP | 443 | `0.0.0.0/0` | (Optional) If using SSL |

### Outbound Rules:

| Type | Protocol | Port | Destination | Purpose |
|------|----------|------|-------------|---------|
| All Traffic | All | All | `0.0.0.0/0` | Allow all outbound (default) |

### Security Best Practices:

1. **Restrict SSH access** to your IP only, not `0.0.0.0/0`
2. **Restrict port 5000** to Backend server IP in production
3. Use a **dedicated Security Group** for the Judge service
4. Enable **VPC Flow Logs** for monitoring

---

## 3. Why Docker is Required on EC2

Docker is required for these reasons:

1. **Consistency**: Same container runs locally and on EC2 - no "works on my machine" issues

2. **Isolation**: User code runs inside a container, providing a security boundary

3. **Easy Deployment**: Just pull the image and run - no Python version conflicts

4. **Resource Control**: Docker allows CPU/memory limits on containers

5. **Easy Updates**: Deploy new versions by pulling new image tags

6. **Portability**: Can migrate to ECS, EKS, or other cloud providers easily

---

## 4. Storage Requirements

| Component | Size | Purpose |
|-----------|------|---------|
| Root Volume | 20 GB (gp3) | OS + Docker images |
| Docker Image | ~200 MB | Python slim + dependencies |
| Temp Files | Minimal | User code execution (cleaned up) |

**Volume Type:** gp3 (General Purpose SSD)
- Better price-performance than gp2
- Sufficient IOPS for the workload

---

## 5. Post-Launch Steps (What Will Be Done)

After the EC2 instance is launched, these steps will be performed:

### Step 5.1: Connect to EC2
```bash
ssh -i "your-key.pem" ec2-user@<ec2-public-ip>
```

### Step 5.2: Install Docker
```bash
# Amazon Linux 2023
sudo dnf update -y
sudo dnf install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user
```

### Step 5.3: Transfer Docker Image (Option A - Build on EC2)
```bash
# Clone or copy files to EC2
# Build image on EC2
docker build -t judge-service:1.0 .
```

### Step 5.3: Transfer Docker Image (Option B - Docker Hub)
```bash
# On local machine: Push to Docker Hub
docker tag judge-service:1.0 yourusername/judge-service:1.0
docker push yourusername/judge-service:1.0

# On EC2: Pull from Docker Hub
docker pull yourusername/judge-service:1.0
```

### Step 5.4: Run the Container
```bash
docker run -d \
  --name judge-service \
  --restart unless-stopped \
  -p 5000:5000 \
  judge-service:1.0
```

### Step 5.5: Verify Deployment
```bash
# Check container status
docker ps

# Test health endpoint
curl http://localhost:5000/health

# Test from outside (using public IP)
curl http://<ec2-public-ip>:5000/health
```

### Step 5.6: Update Backend Configuration
Update the Java Backend's `application.yml` to point to EC2:
```yaml
judge:
  service:
    url: http://<ec2-public-ip>:5000
```

---

## 6. Cost Estimation (Monthly)

| Resource | Type | Estimated Cost |
|----------|------|----------------|
| EC2 t3.micro | On-Demand | ~$8/month |
| EC2 t3.small | On-Demand | ~$15/month |
| EBS 20GB gp3 | Storage | ~$2/month |
| Data Transfer | Outbound | Varies |

**Total Estimate:** $10-20/month for a basic setup

**Cost Optimization:**
- Use Reserved Instances for 30-40% savings
- Use Spot Instances for non-critical workloads
- t3.micro is Free Tier eligible (750 hours/month for 12 months)

---

## 7. Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         AWS CLOUD                           │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    VPC                               │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │              Public Subnet                   │    │   │
│  │  │                                              │    │   │
│  │  │  ┌────────────────────────────────────────┐ │    │   │
│  │  │  │           EC2 Instance                 │ │    │   │
│  │  │  │         (t3.small)                     │ │    │   │
│  │  │  │                                        │ │    │   │
│  │  │  │  ┌──────────────────────────────────┐ │ │    │   │
│  │  │  │  │      Docker Container            │ │ │    │   │
│  │  │  │  │                                  │ │ │    │   │
│  │  │  │  │  ┌────────────────────────────┐ │ │ │    │   │
│  │  │  │  │  │    Judge Service           │ │ │ │    │   │
│  │  │  │  │  │    (Python/Flask)          │ │ │ │    │   │
│  │  │  │  │  │    Port 5000               │ │ │ │    │   │
│  │  │  │  │  └────────────────────────────┘ │ │ │    │   │
│  │  │  │  │                                  │ │ │    │   │
│  │  │  │  └──────────────────────────────────┘ │ │    │   │
│  │  │  │                                        │ │    │   │
│  │  │  └────────────────────────────────────────┘ │    │   │
│  │  │                     ▲                       │    │   │
│  │  └─────────────────────│───────────────────────┘    │   │
│  │                        │                             │   │
│  │  ┌─────────────────────│───────────────────────┐    │   │
│  │  │          Security Group                      │    │   │
│  │  │          - Port 22 (SSH)                     │    │   │
│  │  │          - Port 5000 (Judge API)             │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────┘   │
│                           ▲                                 │
└───────────────────────────│─────────────────────────────────┘
                            │
                            │ HTTP POST /judge
                            │
                   ┌────────┴────────┐
                   │  Java Backend   │
                   │  (Your Server)  │
                   └─────────────────┘
```

---

## 8. Checklist Before Deployment

- [ ] AWS Account created and configured
- [ ] Key pair created for SSH access
- [ ] Security Group created with proper rules
- [ ] Docker image tested locally
- [ ] Backend ready to be reconfigured
- [ ] (Optional) Domain name for the Judge service
- [ ] (Optional) SSL certificate for HTTPS

---

## 9. Next Steps

1. Launch EC2 instance with specifications above
2. Install Docker on EC2
3. Deploy the Judge container
4. Update Backend configuration
5. Test end-to-end flow
6. (Optional) Set up monitoring with CloudWatch
7. (Optional) Configure auto-restart with systemd

---

**DO NOT PROCEED WITH AWS DEPLOYMENT YET.**
This document is for planning purposes only.

