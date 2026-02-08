# ============================================================
# JUDGE SERVICE - DOCKERFILE
# ============================================================
# This Dockerfile creates a container for the Python Judge service
# that executes user-submitted code and evaluates test cases.
# ============================================================

# Use Python 3.10 slim image (smaller size, production-ready)
FROM python:3.10-slim

# Set working directory inside container
WORKDIR /app

# Set environment variables
# PYTHONDONTWRITEBYTECODE: Prevents Python from writing .pyc files
# PYTHONUNBUFFERED: Ensures Python output is sent directly to terminal (no buffering)
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Copy requirements first (Docker layer caching optimization)
# If requirements don't change, this layer is cached
COPY requirements.txt .

# Install Python dependencies
# --no-cache-dir: Don't store pip cache (reduces image size)
RUN pip install --no-cache-dir -r requirements.txt

# Copy the judge application code
COPY judge.py .
COPY testcases.py .

# Expose port 5000 for the Flask application
EXPOSE 5000

# Health check to verify container is running correctly
# Checks /health endpoint every 30 seconds
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:5000/health')" || exit 1

# Run the application using gunicorn (production WSGI server)
# --bind 0.0.0.0:5000: Listen on all interfaces, port 5000
# --workers 2: Number of worker processes (2 is good for a small instance)
# --timeout 30: Request timeout (allows time for code execution)
# judge:app: Module name and Flask app variable
CMD ["gunicorn", "--bind", "0.0.0.0:5000", "--workers", "2", "--timeout", "30", "judge:app"]
