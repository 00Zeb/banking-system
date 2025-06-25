#!/bin/bash

# Banking System Startup Script
echo "🏦 Starting Banking System with Docker Compose..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose > /dev/null 2>&1; then
    echo "❌ docker-compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "✅ Docker is running"
echo "✅ Docker Compose is available"
echo ""

# Build and start the services
echo "🔨 Building and starting services..."
docker-compose up --build

echo ""
echo "🎉 Banking System should now be running!"
echo ""
echo "📱 Access the web interface at: http://localhost:3000"
echo "🔧 Access the API directly at: http://localhost:8080"
echo ""
echo "To stop the system, press Ctrl+C or run: docker-compose down"
