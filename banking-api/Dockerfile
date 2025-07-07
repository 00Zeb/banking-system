# Banking API Dockerfile
FROM openjdk:21-jdk-slim

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the entire project structure
COPY . .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Build the parent project first to ensure proper dependency resolution
RUN ./mvnw clean install -DskipTests

# Build the banking-api with Spring Boot plugin to create executable JAR
RUN ./mvnw clean package -pl banking-api -DskipTests

# Expose the port that banking-api runs on
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker

# Create a directory for banking data persistence
RUN mkdir -p /app/data
VOLUME ["/app/data"]

# Run the banking-api
CMD ["java", "-jar", "banking-api/target/banking-api-1.0-SNAPSHOT.jar"]
