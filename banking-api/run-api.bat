@echo off
echo Building and Running Banking REST API...
echo.

REM First, build the core banking application
echo Step 1: Building Banking Core Application...
cd ..
call mvnw.cmd clean install -q
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to build banking core application
    pause
    exit /b 1
)

REM Then build and run the API
echo Step 2: Building Banking API...
cd banking-api
call mvnw.cmd clean compile -q
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to build banking API
    pause
    exit /b 1
)

echo Step 3: Starting Banking REST API...
echo.
echo API will be available at:
echo - Main API: http://localhost:8080/api/v1/banking
echo - Swagger UI: http://localhost:8080/swagger-ui.html
echo - Health Check: http://localhost:8080/actuator/health
echo.
echo Press Ctrl+C to stop the server
echo.

call mvnw.cmd spring-boot:run
