@echo off
echo ========================================
echo Starting Banking API with Process Integration
echo ========================================
echo.

echo Step 1: Building banking-application...
call mvnw clean install -f banking-application/pom.xml -q
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to build banking-application
    pause
    exit /b 1
)

echo Step 2: Building banking-api...
call mvnw clean package -f banking-api/pom.xml -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to build banking-api
    pause
    exit /b 1
)

echo.
echo Step 3: Starting Banking API...
echo The API will interact with banking-application as a separate process
echo.
echo Available endpoints:
echo - POST /api/v1/banking/register - Register new user
echo - POST /api/v1/banking/login - Authenticate user
echo - POST /api/v1/banking/deposit - Deposit money
echo - POST /api/v1/banking/withdraw - Withdraw money
echo - POST /api/v1/banking/balance - Get account balance
echo - POST /api/v1/banking/transactions - Get transaction history
echo.
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo.

cd banking-api
java -jar target/banking-api-1.0-SNAPSHOT.jar
