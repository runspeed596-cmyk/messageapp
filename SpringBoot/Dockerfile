# ============================================
# Runtime-only image (JAR pre-built locally)
# ============================================
# Iranian servers cannot download Gradle dependencies (sanctions).
# Build the JAR locally and include it in the deployment ZIP.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser

# Create uploads directory
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

# Copy the pre-built JAR
COPY build/libs/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose Spring Boot port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", \
  "-Xms512m", "-Xmx2g", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
