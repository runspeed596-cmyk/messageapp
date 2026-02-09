# ============================================
# Stage 1: Build the application
# ============================================
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and build files first for better caching
COPY gradle/ gradle/
COPY gradlew .
COPY gradlew.bat .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy source code
COPY src/ src/

# Build the application (skip tests for faster build)
RUN gradle bootJar --no-daemon -x test

# ============================================
# Stage 2: Create the runtime image
# ============================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser

# Create uploads directory
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose Spring Boot port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
