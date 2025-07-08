# CI/CD Setup for Banking System

This document describes the CI/CD pipeline implemented for the Banking System using GitHub Actions and Railway.app.

## 🚀 Quick Start

1. **Initial Setup**: Railway.app is already connected to your GitHub repository
2. **Automatic Deployment**: Every push to `main` branch triggers deployment
3. **Health Checks**: Use `./health-check.sh [environment]` to verify service health
4. **Status Reports**: Use `./status-report.sh` to get comprehensive system status

## 📋 Available Scripts

```bash
# Health checks
./health-check.sh local      # Check local services
./health-check.sh staging    # Check staging environment
./health-check.sh production # Check production environment

# Deployment
./deploy.sh staging         # Prepare staging deployment
./deploy.sh production      # Prepare production deployment

# Status reporting
./status-report.sh          # Full system status report
./status-report.sh staging  # Environment-specific status
```

## 🔄 CI/CD Pipeline

### Continuous Integration (.github/workflows/ci.yml)
- **Triggers**: Push to main/develop, Pull Requests
- **Steps**:
  1. Checkout code
  2. Setup Java 21
  3. Cache Maven dependencies
  4. Run all tests (25 test cases)
  5. Generate test reports
  6. Build application artifacts
  7. Upload build artifacts

### Deployment (.github/workflows/deploy.yml)
- **Staging**: Automatic deployment on main branch push
- **Production**: Manual deployment via workflow_dispatch
- **Features**:
  - Environment-specific builds
  - Health checks after deployment
  - Deployment notifications

### Monitoring (.github/workflows/monitoring.yml)
- **Schedule**: Every 5 minutes
- **Checks**: API health, web application, performance metrics
- **Alerts**: Failure notifications

## 🌍 Environments

### Local Development
- **API**: http://localhost:8080
- **Web**: http://localhost:3000
- **Profile**: default
- **Health**: http://localhost:8080/actuator/health

### Staging
- **API**: https://your-staging-api.railway.app
- **Web**: https://your-staging-web.railway.app
- **Profile**: staging
- **Features**: Enhanced logging, debug endpoints

### Production
- **API**: https://your-production-api.railway.app
- **Web**: https://your-production-web.railway.app
- **Profile**: production
- **Features**: Optimized performance, minimal logging

## 📊 Monitoring & Health Checks

### Available Endpoints
- `/actuator/health` - Service health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Performance metrics
- `/actuator/prometheus` - Prometheus metrics (staging/production)

### Health Check Script
```bash
./health-check.sh [environment]
```

Checks:
- Service availability
- Health endpoint status
- Application info
- Basic metrics

## 🔧 Configuration Files

### Railway.app Configuration
- `railway.toml` - Railway deployment configuration
- `Procfile` - Process definitions
- `application-staging.yml` - Staging environment settings
- `application-production.yml` - Production environment settings

### GitHub Actions
- `.github/workflows/ci.yml` - CI pipeline
- `.github/workflows/deploy.yml` - Deployment workflows
- `.github/workflows/monitoring.yml` - Health monitoring
- `.github/workflows/notifications.yml` - Deployment notifications

## 🚨 Troubleshooting

### Common Issues

1. **Build Failures**
   - Check Java version (requires Java 21)
   - Verify Maven dependencies
   - Run `./mvnw clean install` locally

2. **Deployment Issues**
   - Check Railway dashboard for deployment logs
   - Verify environment variables
   - Check application-specific configuration

3. **Health Check Failures**
   - Verify service URLs
   - Check network connectivity
   - Review application logs

### Getting Help

1. Check GitHub Actions logs for CI/CD issues
2. Review Railway dashboard for deployment status
3. Run `./status-report.sh` for comprehensive system status
4. Use `./health-check.sh` for service-specific diagnostics

## 📈 Next Steps

1. **Update Railway URLs**: Replace placeholder URLs with actual Railway service URLs
2. **Add Notifications**: Configure Slack/Discord/email notifications
3. **Enhance Monitoring**: Add custom metrics and alerts
4. **Security**: Add secrets management for production
5. **Performance**: Optimize build times and resource usage

## 🔗 Useful Links

- [Railway.app Dashboard](https://railway.app/dashboard)
- [GitHub Actions](https://github.com/your-username/banking-system/actions)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Maven Documentation](https://maven.apache.org/guides/)