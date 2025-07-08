#!/bin/bash

# Banking System Deployment Script for Railway.app
# Usage: ./deploy.sh [staging|production]

set -e

ENVIRONMENT=${1:-staging}
PROJECT_ROOT=$(pwd)

echo "🚀 Starting deployment for environment: $ENVIRONMENT"

# Validate environment
if [[ "$ENVIRONMENT" != "staging" && "$ENVIRONMENT" != "production" ]]; then
    echo "❌ Invalid environment. Use 'staging' or 'production'"
    exit 1
fi

# Build the application
echo "📦 Building application..."
./mvnw clean install -DskipTests -Dspring.profiles.active=$ENVIRONMENT

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✅ Build completed successfully"
else
    echo "❌ Build failed"
    exit 1
fi

# Railway deployment (automatic via git push)
echo "🚂 Railway will handle deployment automatically on git push"
echo "📍 Environment: $ENVIRONMENT"
echo "🔧 Profile: $ENVIRONMENT"

# Display deployment info
echo ""
echo "🔍 Deployment Information:"
echo "Environment: $ENVIRONMENT"
echo "Spring Profile: $ENVIRONMENT"
echo "JAR Files:"
echo "  - banking-application/target/banking-application-1.0-SNAPSHOT.jar"
echo "  - banking-api/target/banking-api-1.0-SNAPSHOT.jar"
echo "  - banking-web/target/banking-web-1.0-SNAPSHOT.war"

echo ""
echo "🌐 Expected URLs (update with your actual Railway URLs):"
if [ "$ENVIRONMENT" == "staging" ]; then
    echo "  API: https://your-staging-api.railway.app"
    echo "  Web: https://your-staging-web.railway.app"
    echo "  Health: https://your-staging-api.railway.app/actuator/health"
else
    echo "  API: https://your-production-api.railway.app"
    echo "  Web: https://your-production-web.railway.app"
    echo "  Health: https://your-production-api.railway.app/actuator/health"
fi

echo ""
echo "✅ Deployment preparation complete!"
echo "💡 Push to your repository to trigger Railway deployment"