# Dockerfile for pre-built Spring Boot Calculator
FROM eclipse-temurin:8-jre-alpine-3.22

# Set working directory
WORKDIR /app

# Copy the pre-built JAR file
COPY target/*.jar app.jar

# Create non-root user for security
RUN addgroup -g 1001 appuser && adduser -u 1001 -G appuser -s /bin/sh -D appuser
RUN chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]