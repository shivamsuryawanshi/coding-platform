# CodeNexus Backend
# Spring Boot + MySQL + AWS S3
# Port: 8080

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install curl for health check
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Environment variables (set at runtime)
ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_NAME=coding_platform
ENV DB_USER=admin
ENV DB_PASSWORD=
ENV AWS_REGION=eu-north-1
ENV S3_BUCKET=coding-platform-testcases
ENV JUDGE_HOST=localhost
ENV JUDGE_PORT=5000

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# Run
ENTRYPOINT ["java", "-jar", "app.jar"]
