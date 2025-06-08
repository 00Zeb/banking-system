@echo off
echo ========================================
echo Starting Banking Web Application
echo ========================================
echo.

echo Step 1: Building the web application...
call mvnw clean package -f banking-web/pom.xml -q
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to build banking-web
    pause
    exit /b 1
)

echo.
echo Step 2: Starting Jetty server...
echo.
echo The web application will be available at:
echo http://localhost:3000
echo.
echo Make sure the Banking API is running on http://localhost:8080
echo You can start it with: start-banking-api.bat
echo.
echo Press Ctrl+C to stop the server
echo.

cd banking-web
call ../mvnw jetty:run
