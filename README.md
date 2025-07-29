# Java Spring Boot Services with Docker Compose & Atatus Monitoring

This project contains two Spring Boot services that communicate with each other, featuring APM monitoring with Atatus and optimized multi-stage Docker builds.

- **Service 1** (Port 3501): Main service that can call Service 2
- **Service 2** (Port 3502): Secondary service that provides data

## Features

### Service Endpoints
Each service has three endpoints:
- `/` - Returns service information with environment details
- `/data` - Returns random data with timestamps
- `/call` - Service 1 calls Service 2's `/data` endpoint (and vice versa)

### Monitoring & Observability
- **Atatus APM Integration** - Full application performance monitoring
- **Distributed Tracing** - End-to-end request tracking between services
- **JVM Metrics** - Memory, GC, and thread monitoring
- **Error Tracking** - Exception monitoring with stack traces

### Docker Optimization
- **Multi-stage builds** - Optimized image sizes (~70% reduction)
- **JDK 21** - Latest Java version with performance improvements
- **Maven wrapper** - Automatic dependency management
- **Environment-based configuration** - Flexible deployment settings

## Prerequisites

- Docker & Docker Compose
- Atatus Java agent JAR file (see setup below)

## Setup Instructions

### 1. Atatus Agent Setup
Download the Atatus Java agent and place it in both service directories:

```bash
# Download Atatus Java agent
wget https://s3.amazonaws.com/download.atatus.com/atatus-java/atatus-java-agent-latest.jar

# Copy to both services
cp atatus-java-agent-latest.jar service1/atatus-java-agent.jar
cp atatus-java-agent-latest.jar service2/atatus-java-agent.jar
```

### 2. Environment Configuration
The project uses environment variables for configuration. The `.env` file contains Atatus configuration:

```bash
# Atatus configuration is already in .env
# Update ATATUS_LICENSE_KEY with your actual license key
```

### Atatus Environment Variables

| Variable | Description | Current Value |
|----------|-------------|---------------|
| `ATATUS_LICENSE_KEY` | Your Atatus license key | `<your-license-key>` |
| `ATATUS_DEBUG` | Enable debug logging | `true` |
| `ATATUS_ENABLE` | Enable/disable monitoring | `true` |
| `ATATUS_LOG_LEVEL` | Agent log level | `info` |
| `ATATUS_TRACING` | Distributed tracing | `true` |
| `ATATUS_ANALYTICS` | Analytics collection | `true` |
| `ATATUS_ENVIRONMENT` | Environment identifier | `Production` |
| `ATATUS_NOTIFY_HOST` | Atatus server URL | `https://demo.atatus.com` |

### Service Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Service port (from Docker) | `3501/3502` |
| `SERVICE2_URL` | Service 2 endpoint for Service 1 | `http://service2:3502` |
| `SERVICE_1_URL` | Service 1 endpoint for Service 2 | `http://service1:3501` |
| `ATATUS_APP_NAME` | Service name in Atatus | `JPT-Service-1/2` |

## Running the Services

### Quick Start
```bash
# Ensure you have the Atatus agent files in place
ls service1/atatus-java-agent.jar service2/atatus-java-agent.jar

# Build and start both services
docker-compose up --build
```

### The services will be available at:
- **Service 1**: http://localhost:3501
- **Service 2**: http://localhost:3502
- **Atatus Dashboard**: https://demo.atatus.com (with your credentials)

## Testing the Endpoints

### Service 1 Endpoints:
```bash
# Basic endpoint - shows service info and environment
curl http://localhost:3501/

# Get random data from Service 1
curl http://localhost:3501/data

# Call Service 2 from Service 1 (creates distributed trace)
curl http://localhost:3501/call
```

### Service 2 Endpoints:
```bash
# Basic endpoint - shows service info and environment
curl http://localhost:3502/

# Get random data from Service 2
curl http://localhost:3502/data

# Call Service 1 from Service 2 (creates distributed trace)
curl http://localhost:3502/call
```

## Monitoring & Debugging

### Atatus Dashboard
1. **Login** to https://demo.atatus.com with your credentials
2. **View Applications**: You'll see `JPT-Service-1` and `JPT-Service-2`
3. **Distributed Traces**: Monitor `/call` endpoints for cross-service traces
4. **Performance Metrics**: View response times, throughput, and errors

### Container Logs
```bash
# View all services
docker-compose logs

# View specific service
docker-compose logs service1
docker-compose logs service2

# Follow logs in real-time
docker-compose logs -f
```

### Verify Atatus Integration
Check container logs for Atatus initialization messages:
```bash
# Look for Atatus startup messages
docker-compose logs | grep -i atatus
```

## Architecture

### Technology Stack
- **Java 21** - Latest LTS Java version
- **Spring Boot 3.2.0** - Modern Spring framework
- **Maven** - Dependency management with wrapper
- **Docker Multi-stage** - Optimized container builds
- **Atatus APM** - Application performance monitoring

### Service Communication
- Services communicate via HTTP within Docker network
- Each service runs on its designated port (3501, 3502)
- Docker Compose manages networking and dependencies
- Environment variables handle service discovery

### Docker Architecture
```
Build Stage (openjdk:21-jdk-slim):
├── Maven wrapper setup
├── Dependency download
├── Source compilation
└── JAR packaging

Runtime Stage (openjdk:21-jdk-slim):
├── Atatus Java agent
├── Built JAR file
└── Minimal runtime environment
```

## Troubleshooting

### Common Issues

1. **Atatus agent missing**:
   ```bash
   # Ensure agent JAR exists in both directories
   ls -la service*/atatus-java-agent.jar
   ```

2. **Maven wrapper errors**:
   ```bash
   # Clean rebuild
   docker-compose down
   docker-compose up --build --no-cache
   ```

3. **Service communication errors**:
   ```bash
   # Check Docker network
   docker network ls
   docker network inspect java-propagation-test_app-network
   ```

## Stopping the Services

```bash
# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Stop and remove all (including images)
docker-compose down --rmi all -v
```

## Development Notes

- Services automatically detect environment variables from Docker
- Atatus traces inter-service communication automatically
- Multi-stage builds reduce final image size significantly
- JVM performance is optimized for containerized environments
