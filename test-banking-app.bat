@echo off
echo Testing banking application JAR...
echo.

echo Building banking-application...
call mvnw clean install -f banking-application/pom.xml -q
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to build banking-application
    exit /b 1
)

echo.
echo Testing JAR execution...
echo 2 | echo testuser | echo testpass | echo 3 | java -jar banking-application/target/banking-application-1.0-SNAPSHOT.jar

echo.
echo Done.
