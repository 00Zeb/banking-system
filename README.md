# Banking System - Multi-Module Project

A comprehensive banking system built as a multi-module Maven project with a standalone CLI application and a REST API wrapper.

## 🏗️ Project Structure

```
banking-system/
├── pom.xml                    # Parent POM with dependency management
├── banking-application/       # Core banking application (CLI)
│   ├── src/
│   │   ├── main/java/        # Core banking logic
│   │   └── test/java/        # Unit tests
│   └── pom.xml
├── banking-api/              # REST API wrapper
│   ├── src/
│   │   ├── main/java/        # Spring Boot REST API
│   │   └── test/java/        # API tests
│   └── pom.xml
└── README.md
```

## 📦 Modules

### 🏦 banking-application
- **Purpose**: Core banking application with CLI interface
- **Technology**: Pure Java with JUnit 5 testing
- **Features**: 
  - User management and authentication
  - Account operations (deposit, withdraw, balance)
  - Transaction history
  - File-based persistence
  - Comprehensive unit tests

### 🌐 banking-api
- **Purpose**: REST API wrapper around the core banking application
- **Technology**: Spring Boot 3.2.0
- **Features**:
  - RESTful endpoints for all banking operations
  - Input validation and error handling
  - API documentation with Swagger/OpenAPI
  - Health checks and monitoring
  - Comprehensive integration tests

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Building the Entire System

```bash
# Build all modules
./mvnw clean install

# Run tests for all modules
./mvnw test
```

### Running the CLI Application

```bash
# Navigate to banking-application
cd banking-application

# Run the standalone application
java -jar target/banking-application-1.0-SNAPSHOT.jar

# Or use Maven
./mvnw exec:java -Dexec.mainClass="com.example.banking.BankingApp"
```

### Running the REST API

```bash
# Navigate to banking-api
cd banking-api

# Start the Spring Boot application
./mvnw spring-boot:run
```

The API will be available at:
- **Main API**: http://localhost:8080/api/v1/banking
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

## 🔧 Development

### Module Dependencies
- `banking-api` depends on `banking-application`
- Both modules inherit from the parent POM
- Dependency versions are managed centrally

### Building Individual Modules

```bash
# Build only the core application
cd banking-application
../mvnw clean install

# Build only the API (requires core to be built first)
cd banking-api
../mvnw clean install
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run tests for specific module
cd banking-application
../mvnw test

cd banking-api
../mvnw test
```

## 📋 API Endpoints

### Authentication
- `POST /api/v1/banking/register` - Register new user
- `POST /api/v1/banking/login` - Authenticate user

### Banking Operations
- `POST /api/v1/banking/deposit` - Deposit money
- `POST /api/v1/banking/withdraw` - Withdraw money
- `POST /api/v1/banking/balance` - Get account balance
- `POST /api/v1/banking/transactions` - Get transaction history
- `DELETE /api/v1/banking/account` - Delete account

## 🏛️ Architecture Benefits

### ✅ **Separation of Concerns**
- **Core Logic**: Pure Java business logic in `banking-application`
- **API Layer**: Spring Boot REST interface in `banking-api`
- **Clean Dependencies**: API depends on core, not vice versa

### ✅ **Maintainability**
- **Independent Development**: Modules can be developed separately
- **Centralized Management**: Dependencies managed in parent POM
- **Version Consistency**: All modules use same version

### ✅ **Deployment Flexibility**
- **Standalone JAR**: Core application can run independently
- **REST API**: Can be deployed as web service
- **Multiple Interfaces**: Easy to add new interfaces (mobile, web UI)

### ✅ **Testing Strategy**
- **Unit Tests**: Core business logic tested in isolation
- **Integration Tests**: API endpoints tested with real core
- **Comprehensive Coverage**: Both modules have extensive test suites

## 🔄 Build Lifecycle

1. **Parent POM**: Defines dependency management and plugin configuration
2. **banking-application**: Builds first (no dependencies)
3. **banking-api**: Builds second (depends on banking-application)

## 📊 Technology Stack

| Module | Technology | Purpose |
|--------|------------|---------|
| Parent | Maven | Dependency management |
| banking-application | Java 17, JUnit 5, AssertJ | Core business logic |
| banking-api | Spring Boot 3.2, OpenAPI | REST API wrapper |

## 🚀 Future Enhancements

- **banking-web**: Web frontend module (React/Angular)
- **banking-mobile**: Mobile API module
- **banking-batch**: Batch processing module
- **banking-security**: Enhanced security module

## 🤝 Contributing

1. Build the entire project: `./mvnw clean install`
2. Make changes to appropriate module
3. Run tests: `./mvnw test`
4. Ensure all modules build successfully

## 📄 License

This project is licensed under the MIT License.
