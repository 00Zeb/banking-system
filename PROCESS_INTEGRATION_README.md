# Banking API - Process Integration Architecture

## Overview

The banking-api has been modified to interact with the banking-application as a running JAR file instead of using it as a Java dependency. This approach keeps the original banking-application completely unchanged while providing REST API access to its functionality.

## Architecture Changes

### Before (Dependency-based)
```
banking-api → (imports) → banking-application classes
```

### After (Process-based)
```
banking-api → (process calls) → banking-application JAR
```

## Key Components

### 1. BankingProcessService
- **Location**: `banking-api/src/main/java/com/example/banking/api/service/BankingProcessService.java`
- **Purpose**: Manages process execution and communication with banking-application JAR
- **Features**:
  - Launches banking-application as separate process for each operation
  - Communicates via stdin/stdout
  - Parses CLI output to extract results
  - Handles process lifecycle and cleanup

### 2. Configuration
- **Location**: `banking-api/src/main/resources/application.yml`
- **Properties**:
  ```yaml
  banking:
    application:
      jar-path: "../banking-application/target/banking-application-1.0-SNAPSHOT.jar"
      process-timeout: 30000  # 30 seconds
      java-command: "java"
  ```

### 3. Model Classes
- **BankingUser**: Internal representation of user data
- **BankingTransaction**: Internal representation of transaction data
- **Purpose**: Replace direct imports from banking-application

## How It Works

### User Registration Flow
1. API receives POST request to `/api/v1/banking/register`
2. BankingProcessService launches banking-application JAR
3. Sends commands via stdin: `2\n{username}\n{password}\n3\n`
4. Parses stdout for success/failure messages
5. Returns result to API client

### Authentication Flow
1. API receives POST request to `/api/v1/banking/login`
2. BankingProcessService launches banking-application JAR
3. Sends commands via stdin: `1\n{username}\n{password}\n4\n3\n`
4. Parses stdout for authentication success
5. Returns user data to API client

### Transaction Flow (Deposit/Withdraw)
1. API receives transaction request
2. BankingProcessService launches banking-application JAR
3. Authenticates user first
4. Performs transaction operation
5. Parses output for success/failure and balance
6. Returns transaction result to API client

## Benefits

1. **Zero Changes to Original Application**: banking-application remains completely unchanged
2. **Process Isolation**: Each operation runs in a separate process, ensuring clean state
3. **Data Consistency**: Uses the same persistence mechanism as the original application
4. **Maintainability**: Original application can be updated independently

## Limitations

1. **Performance**: Process creation overhead for each operation
2. **Parsing Complexity**: Relies on parsing CLI output which could be fragile
3. **Error Handling**: Limited error information from CLI interface
4. **Feature Limitations**: Some operations (like user deletion) are not available in the CLI

## Building and Running

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Build Steps
```bash
# Build banking-application first
./mvnw clean install -f banking-application/pom.xml

# Build banking-api
./mvnw clean package -f banking-api/pom.xml -DskipTests

# Start the API
cd banking-api
java -jar target/banking-api-1.0-SNAPSHOT.jar
```

### Quick Start Script
```bash
# Windows
start-banking-api.bat

# The API will be available at http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

## Testing

### Integration Tests
Run integration tests with:
```bash
./mvnw test -f banking-api/pom.xml -Dintegration.tests=true
```

### Manual Testing
Use the Swagger UI at `http://localhost:8080/swagger-ui.html` to test the API endpoints.

## Configuration

### JAR Path Configuration
Update `banking.application.jar-path` in `application.yml` if the banking-application JAR is in a different location.

### Timeout Configuration
Adjust `banking.application.process-timeout` if operations need more time to complete.

## Future Improvements

1. **Connection Pooling**: Maintain long-running processes instead of creating new ones
2. **Better Error Handling**: Enhanced parsing of error messages
3. **Performance Optimization**: Batch operations or persistent connections
4. **Health Checks**: Monitor banking-application availability
5. **Logging**: Enhanced logging of process interactions
