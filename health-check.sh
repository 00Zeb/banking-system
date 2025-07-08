#!/bin/bash

# Banking System Health Check Script
# Usage: ./health-check.sh [staging|production|local]

set -e

ENVIRONMENT=${1:-local}
PROJECT_ROOT=$(pwd)

echo "🔍 Health Check for Banking System - Environment: $ENVIRONMENT"

# Define URLs based on environment
case $ENVIRONMENT in
    "local")
        API_URL="http://localhost:8080"
        WEB_URL="http://localhost:3000"
        ;;
    "staging")
        API_URL="https://your-staging-api.railway.app"
        WEB_URL="https://your-staging-web.railway.app"
        ;;
    "production")
        API_URL="https://your-production-api.railway.app"
        WEB_URL="https://your-production-web.railway.app"
        ;;
    *)
        echo "❌ Invalid environment. Use 'local', 'staging', or 'production'"
        exit 1
        ;;
esac

echo "🌐 Checking URLs:"
echo "  API: $API_URL"
echo "  Web: $WEB_URL"
echo ""

# Function to check URL
check_url() {
    local url=$1
    local name=$2
    
    echo -n "Checking $name... "
    
    if curl -s --max-time 10 --fail "$url" > /dev/null 2>&1; then
        echo "✅ OK"
        return 0
    else
        echo "❌ FAILED"
        return 1
    fi
}

# Function to check health endpoint
check_health() {
    local url=$1
    
    echo -n "Checking health endpoint... "
    
    if response=$(curl -s --max-time 10 --fail "$url/actuator/health" 2>/dev/null); then
        status=$(echo "$response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        if [ "$status" = "UP" ]; then
            echo "✅ UP"
            return 0
        else
            echo "❌ DOWN (status: $status)"
            return 1
        fi
    else
        echo "❌ UNREACHABLE"
        return 1
    fi
}

# Function to get application info
get_app_info() {
    local url=$1
    
    echo "📋 Application Information:"
    if info=$(curl -s --max-time 10 --fail "$url/actuator/info" 2>/dev/null); then
        echo "$info" | python3 -m json.tool 2>/dev/null || echo "$info"
    else
        echo "  ❌ Could not retrieve application info"
    fi
}

# Function to check metrics
check_metrics() {
    local url=$1
    
    echo "📊 Basic Metrics:"
    if metrics=$(curl -s --max-time 10 --fail "$url/actuator/metrics" 2>/dev/null); then
        echo "  Available metrics endpoint: ✅"
        # You can parse specific metrics here
    else
        echo "  ❌ Could not retrieve metrics"
    fi
}

# Perform health checks
echo "🔍 Starting health checks..."
echo ""

# Check main API endpoint
check_url "$API_URL" "API Base URL"

# Check health endpoint
check_health "$API_URL"

# Check web application
check_url "$WEB_URL" "Web Application"

echo ""

# Get additional information for API
get_app_info "$API_URL"

echo ""

# Check metrics
check_metrics "$API_URL"

echo ""
echo "🏁 Health check completed for $ENVIRONMENT environment"

# Return appropriate exit code
if check_url "$API_URL" "API" && check_health "$API_URL" && check_url "$WEB_URL" "Web"; then
    echo "✅ All services are healthy"
    exit 0
else
    echo "❌ Some services are unhealthy"
    exit 1
fi