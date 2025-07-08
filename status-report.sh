#!/bin/bash

# Banking System Status Report Generator
# Usage: ./status-report.sh [environment]

set -e

ENVIRONMENT=${1:-all}
PROJECT_ROOT=$(pwd)
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

echo "📊 Banking System Status Report"
echo "Generated: $TIMESTAMP"
echo "Environment: $ENVIRONMENT"
echo "======================================="
echo ""

# Function to get git info
get_git_info() {
    echo "📋 Git Information:"
    echo "  Branch: $(git branch --show-current)"
    echo "  Commit: $(git rev-parse --short HEAD)"
    echo "  Author: $(git log -1 --pretty=format:'%an <%ae>')"
    echo "  Date: $(git log -1 --pretty=format:'%cd')"
    echo "  Message: $(git log -1 --pretty=format:'%s')"
    echo ""
}

# Function to get build info
get_build_info() {
    echo "🔨 Build Information:"
    if [ -f "banking-api/target/banking-api-1.0-SNAPSHOT.jar" ]; then
        echo "  API JAR: ✅ $(ls -lh banking-api/target/banking-api-1.0-SNAPSHOT.jar | awk '{print $5}')"
    else
        echo "  API JAR: ❌ Not found"
    fi
    
    if [ -f "banking-application/target/banking-application-1.0-SNAPSHOT.jar" ]; then
        echo "  Application JAR: ✅ $(ls -lh banking-application/target/banking-application-1.0-SNAPSHOT.jar | awk '{print $5}')"
    else
        echo "  Application JAR: ❌ Not found"
    fi
    
    if [ -f "banking-web/target/banking-web-1.0-SNAPSHOT.war" ]; then
        echo "  Web WAR: ✅ $(ls -lh banking-web/target/banking-web-1.0-SNAPSHOT.war | awk '{print $5}')"
    else
        echo "  Web WAR: ❌ Not found"
    fi
    echo ""
}

# Function to get test results
get_test_results() {
    echo "🧪 Test Results:"
    if [ -d "banking-api/target/surefire-reports" ]; then
        local test_files=$(find banking-api/target/surefire-reports -name "*.xml" | wc -l)
        echo "  Test reports: $test_files files found"
        
        # Count tests, failures, errors
        if command -v xmllint &> /dev/null; then
            local total_tests=$(find banking-api/target/surefire-reports -name "*.xml" -exec xmllint --xpath "sum(//testsuite/@tests)" {} \; 2>/dev/null | head -1)
            local failures=$(find banking-api/target/surefire-reports -name "*.xml" -exec xmllint --xpath "sum(//testsuite/@failures)" {} \; 2>/dev/null | head -1)
            local errors=$(find banking-api/target/surefire-reports -name "*.xml" -exec xmllint --xpath "sum(//testsuite/@errors)" {} \; 2>/dev/null | head -1)
            
            echo "  Total tests: ${total_tests:-0}"
            echo "  Failures: ${failures:-0}"
            echo "  Errors: ${errors:-0}"
        fi
    else
        echo "  No test reports found - run './mvnw test' first"
    fi
    echo ""
}

# Function to check environment status
check_environment_status() {
    local env=$1
    echo "🌍 Environment Status: $env"
    
    case $env in
        "local")
            echo "  API URL: http://localhost:8080"
            echo "  Web URL: http://localhost:3000"
            ;;
        "staging")
            echo "  API URL: https://your-staging-api.railway.app"
            echo "  Web URL: https://your-staging-web.railway.app"
            ;;
        "production")
            echo "  API URL: https://your-production-api.railway.app"
            echo "  Web URL: https://your-production-web.railway.app"
            ;;
    esac
    
    echo "  Health check: Use './health-check.sh $env' for detailed status"
    echo ""
}

# Function to get deployment status
get_deployment_status() {
    echo "🚀 Deployment Status:"
    echo "  Railway.app: Check your Railway dashboard for current deployment status"
    echo "  GitHub Actions: Check repository Actions tab for CI/CD status"
    echo "  Last deployment: Check git log for recent commits to main branch"
    echo ""
}

# Function to get configuration summary
get_config_summary() {
    echo "⚙️  Configuration Summary:"
    echo "  Spring profiles: local, staging, production"
    echo "  Database: File-based persistence (no external DB required)"
    echo "  Session timeout: 30 minutes"
    echo "  Process timeout: 30-60 seconds (environment dependent)"
    echo "  Health checks: /actuator/health"
    echo "  Monitoring: /actuator/metrics"
    echo ""
}

# Generate report
get_git_info
get_build_info
get_test_results

if [ "$ENVIRONMENT" == "all" ]; then
    check_environment_status "local"
    check_environment_status "staging"
    check_environment_status "production"
else
    check_environment_status "$ENVIRONMENT"
fi

get_deployment_status
get_config_summary

echo "📝 Next Steps:"
echo "  1. Run './health-check.sh [environment]' to verify service health"
echo "  2. Run './deploy.sh [environment]' to deploy to specific environment"
echo "  3. Check Railway dashboard for deployment logs and metrics"
echo "  4. Monitor GitHub Actions for CI/CD pipeline status"
echo ""

echo "======================================="
echo "Report completed: $TIMESTAMP"