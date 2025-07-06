# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Test
```bash
# Build all modules
./mvnw clean install

# Build specific module
./mvnw clean install -f banking-application/pom.xml
./mvnw clean install -f banking-api/pom.xml

# Run all tests
./mvnw test

# Run tests for specific module
./mvnw test -f banking-application/pom.xml
./mvnw test -f banking-api/pom.xml

# Run integration tests (banking-api only)
./mvnw test -f banking-api/pom.xml -Dintegration.tests=true
```

### Run Applications
```bash
# Run CLI application
cd banking-application
java -jar target/banking-application-1.0-SNAPSHOT.jar

# Run REST API
cd banking-api
../mvnw spring-boot:run
# API: http://localhost:8080/api/v1/banking
# Swagger UI: http://localhost:8080/swagger-ui.html

# Run web frontend
cd banking-web
# Open index.html in browser or serve with web server
```

### Quick Start Scripts
```bash
# Build everything
./build-all.bat (Windows)

# Start with Docker
./start-banking-system.sh (Linux/macOS)
docker-compose up --build

# Start individual components
./start-banking-api.bat
./start-banking-web.bat
```

## Architecture Overview

This is a multi-module Maven project implementing a banking system with hexagonal architecture:

### Module Structure
- **banking-application**: Core CLI application with domain logic
- **banking-api**: REST API wrapper using Spring Boot 3.2.0
- **banking-web**: Frontend web interface
- **Parent POM**: Manages dependencies and build configuration

### Hexagonal Architecture (banking-api)
The API module follows hexagonal architecture principles:

**Domain Layer** (`/domain/model/`):
- Pure business logic with no external dependencies
- Rich domain models: Account, User, Transaction, Money
- Embedded business rules and validation

**Application Layer** (`/application/`):
- **Inbound Ports**: Use case interfaces (UserRegistrationUseCase, TransactionUseCase, etc.)
- **Outbound Ports**: Repository and external system interfaces
- **Services**: Business workflow orchestration

**Infrastructure Layer** (`/infrastructure/`):
- **Adapters**: Implement outbound ports (BankingProcessAdapter)
- **Controllers**: Inbound adapters handling HTTP requests

### Process Integration Architecture
The banking-api communicates with banking-application via process execution:
- Each operation launches banking-application JAR as separate process
- Communication via stdin/stdout
- Configured in `application.yml` with jar path and timeout settings

## Key Development Notes

### Testing Strategy
- Unit tests for domain logic in banking-application
- Integration tests for API endpoints in banking-api
- Use `@Test` annotation with JUnit 5
- AssertJ for assertions, Mockito for mocking

### Technology Stack
- Java 17
- Spring Boot 3.2.0 (API only)
- JUnit 5, AssertJ, Mockito
- Maven 3.6+
- OpenAPI/Swagger documentation

### Build Dependencies
1. banking-application must be built first (no dependencies)
2. banking-api depends on banking-application
3. All modules inherit from parent POM

### Configuration Files
- `application.yml`: Banking API configuration including process settings
- `pom.xml`: Maven configuration at root and module levels
- `docker-compose.yml`: Container orchestration setup

### File Persistence
- Banking data stored in text files by banking-application
- No database configuration required
- File location managed by core application

## Common Patterns

### Adding New API Endpoints
1. Define use case interface in `/application/port/in/`
2. Implement use case in `/application/service/`
3. Add controller method in `BankingController`
4. Update process adapter if needed
5. Add integration tests

### Domain Model Changes
1. Modify domain objects in `/domain/model/`
2. Update application services as needed
3. Adjust DTOs for API compatibility
4. Update tests accordingly

### Process Communication
- Commands sent via stdin to banking-application
- Output parsed from stdout for results
- Timeout configured per operation
- Error handling based on output parsing