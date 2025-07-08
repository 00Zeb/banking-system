#!/bin/bash

# Deploy banking-web to static hosting (Netlify, Vercel, etc.)
# This script prepares the web app for static deployment

echo "🌐 Preparing banking-web for static deployment..."

# Create deployment directory
mkdir -p deploy/web

# Copy web assets
cp -r banking-web/src/main/webapp/* deploy/web/

# Update API URLs to point to your Railway API
RAILWAY_API_URL="https://your-banking-api.railway.app"

# Replace localhost API calls with Railway API URL
sed -i "s|http://localhost:8080|$RAILWAY_API_URL|g" deploy/web/js/app.js

echo "✅ Web app prepared for static deployment"
echo "📁 Deploy the 'deploy/web' directory to:"
echo "   - Netlify: Drag & drop to netlify.com"
echo "   - Vercel: vercel.com"
echo "   - GitHub Pages: Enable in repository settings"
echo "   - Railway Static: railway.app"

echo ""
echo "🔧 Don't forget to update RAILWAY_API_URL in this script!"