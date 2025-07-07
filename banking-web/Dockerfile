# Banking Web Dockerfile
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy the entire project structure
COPY . .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Build the banking-web application
RUN ./mvnw clean package -pl banking-web -DskipTests

# Expose the port that banking-web runs on
EXPOSE 3000

# Set environment variables
ENV BANKING_API_URL=http://banking-api:8080

# Run the banking-web using Jetty
CMD ["./mvnw", "jetty:run", "-pl", "banking-web", "-Djetty.port=3000", "-Djetty.host=0.0.0.0"]
