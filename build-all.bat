@echo off
echo ========================================
echo Building Banking System Multi-Module Project
echo ========================================
echo.

echo Step 1: Building all modules...
call mvnw.cmd clean install
if %ERRORLEVEL% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Running all tests...
call mvnw.cmd test
if %ERRORLEVEL% neq 0 (
    echo ERROR: Tests failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.
echo Available artifacts:
echo - banking-application/target/banking-application-1.0-SNAPSHOT.jar
echo - banking-api/target/banking-api-1.0-SNAPSHOT.jar
echo.
echo To run the CLI application:
echo   cd banking-application
echo   java -jar target/banking-application-1.0-SNAPSHOT.jar
echo.
echo To run the REST API:
echo   cd banking-api
echo   ..\mvnw.cmd spring-boot:run
echo   Then visit: http://localhost:8080/swagger-ui.html
echo.
pause
