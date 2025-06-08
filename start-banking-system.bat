@echo off
echo ========================================
echo Banking System - Complete Startup
echo ========================================
echo.
echo This script will start the complete banking system:
echo 1. Banking API (REST API on port 8080)
echo 2. Banking Web (Web UI on port 3000)
echo.

echo Step 1: Building all modules...
call mvnw clean install -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to build the system
    pause
    exit /b 1
)

echo.
echo Step 2: Starting Banking API in background...
start "Banking API" cmd /c "cd banking-api && java -jar target/banking-api-1.0-SNAPSHOT.jar"

echo Waiting for API to start...
timeout /t 10 /nobreak > nul

echo.
echo Step 3: Starting Banking Web Application...
echo.
echo ========================================
echo System URLs:
echo ========================================
echo Banking API: http://localhost:8080
echo Swagger UI:  http://localhost:8080/swagger-ui.html
echo Web App:     http://localhost:3000
echo ========================================
echo.
echo Press Ctrl+C to stop the web server
echo (The API will continue running in the background)
echo.

cd banking-web
call ../mvnw jetty:run
