# Banking REST API

A Spring Boot REST API that wraps the existing Banking Core application, providing HTTP endpoints for all banking operations.

## Architecture

This application follows a clean architecture pattern:
- **API Layer**: Spring Boot REST controllers handle HTTP requests/responses
- **Service Layer**: Delegates operations to the Banking Core application
- **Core Layer**: The existing banking-app JAR (unchanged)

## Features

- ✅ **User Registration & Authentication**
- ✅ **Account Balance Management**
- ✅ **Deposit & Withdrawal Operations**
- ✅ **Transaction History**
- ✅ **Account Deletion**
- ✅ **Input Validation**
- ✅ **Error Handling**
- ✅ **API Documentation (Swagger)**
- ✅ **Health Checks**
- ✅ **Comprehensive Testing**

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Banking Core JAR (banking-app-1.0-SNAPSHOT.jar)

## Getting Started

### 1. Build the Banking Core Application

First, build the core banking application:

```bash
cd ../  # Go to the banking core directory
./mvnw clean install
```

### 2. Build and Run the API

```bash
cd banking-api
./mvnw clean install
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080`

### 3. Access API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs

### 4. Health Check

- **Health Endpoint**: http://localhost:8080/actuator/health

## API Endpoints

### Authentication

#### Register User
```http
POST /api/v1/banking/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure123"
}
```

#### Login
```http
POST /api/v1/banking/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure123"
}
```

### Banking Operations

#### Deposit Money
```http
POST /api/v1/banking/deposit
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure123",
  "amount": 100.50
}
```

#### Withdraw Money
```http
POST /api/v1/banking/withdraw
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure123",
  "amount": 50.25
}
```

#### Get Balance
```http
POST /api/v1/banking/balance
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure123"
}
```

#### Get Transaction History
```http
POST /api/v1/banking/transactions
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure123"
}
```

#### Delete Account
```http
DELETE /api/v1/banking/account
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure123"
}
```

## Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2024-01-15T10:30:00"
}
```

### User Response
```json
{
  "username": "john_doe",
  "balance": 150.75
}
```

### Transaction Response
```json
{
  "type": "Deposit",
  "amount": 100.50,
  "timestamp": "2024-01-15T10:30:00",
  "newBalance": 250.75
}
```

## Testing

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=BankingControllerTest
```

### Test Coverage
The project includes comprehensive tests:
- **Controller Tests**: REST endpoint testing with MockMvc
- **Service Tests**: Business logic testing with real banking core
- **Integration Tests**: End-to-end API testing

## Configuration

### Application Properties
Key configuration options in `application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: banking-api

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### Environment Variables
You can override configuration using environment variables:

```bash
export SERVER_PORT=9090
export SPRING_PROFILES_ACTIVE=production
```

## Security Considerations

⚠️ **Note**: This is a demonstration API. For production use, consider:

- Implementing JWT or OAuth2 authentication
- Adding rate limiting
- Using HTTPS
- Implementing proper session management
- Adding input sanitization
- Using encrypted password storage

## Monitoring

### Health Checks
- **Application Health**: `/actuator/health`
- **Application Info**: `/actuator/info`
- **Metrics**: `/actuator/metrics`

### Logging
Logs are configured to show:
- Request/Response details
- Error traces
- Performance metrics

## Deployment

### JAR Deployment
```bash
./mvnw clean package
java -jar target/banking-api-1.0-SNAPSHOT.jar
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jre-slim
COPY target/banking-api-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Change port in application.yml or use environment variable
   export SERVER_PORT=9090
   ```

2. **Banking Core JAR Not Found**
   ```bash
   # Ensure banking-app is installed in local Maven repository
   cd ../
   ./mvnw clean install
   ```

3. **Data File Permissions**
   ```bash
   # Ensure write permissions for banking_data.ser
   chmod 666 banking_data.ser
   ```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License.
