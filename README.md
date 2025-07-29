# Java Spring Boot Services with Docker Compose

This project contains two Spring Boot services that communicate with each other:

- **Service 1** (Port 3501): Main service that can call Service 2
- **Service 2** (Port 3502): Secondary service that provides data

## Features

Each service has three endpoints:
- `/` - Returns service information
- `/data` - Returns random data
- `/call` - Service 1 calls Service 2's `/data` endpoint

## Prerequisites

- Docker
- Docker Compose

## Configuration

The project uses environment variables for configuration. Copy `.env.example` to `.env` and modify as needed:

```bash
cp .env.example .env
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVICE1_PORT` | External port for Service 1 | `3501` |
| `SERVICE2_PORT` | External port for Service 2 | `3502` |
| `SERVICE1_CONTAINER_NAME` | Container name for Service 1 | `JPT-service-1` |
| `SERVICE2_CONTAINER_NAME` | Container name for Service 2 | `JPT-service-2` |
| `SPRING_PROFILES_ACTIVE` | Spring Boot profile | `default` |
| `JAVA_OPTS` | JVM options | `-Xmx512m -Xms256m` |
| `LOG_LEVEL` | Application log level | `INFO` |
| `ROOT_LOG_LEVEL` | Root log level | `WARN` |

## Running the Services

1. Build and start both services:
```bash
docker-compose up --build
```

2. The services will be available at:
   - Service 1: http://localhost:3501
   - Service 2: http://localhost:3502

## Testing the Endpoints

### Service 1 Endpoints:
```bash
# Basic endpoint
curl http://localhost:3501/

# Get random data from Service 1
curl http://localhost:3501/data

# Call Service 2 from Service 1
curl http://localhost:3501/call
```

### Service 2 Endpoints:
```bash
# Basic endpoint
curl http://localhost:3502/

# Get random data from Service 2
curl http://localhost:3502/data

# Call Service 1 from Service 2
curl http://localhost:3502/call
```

## Architecture

- Both services are built with JDK 21
- Services communicate via HTTP within the Docker network
- Each service runs on its designated port (3501, 3502)
- Docker Compose manages the networking and dependencies
- Environment variables are managed through `.env` file

## Stopping the Services

```bash
docker-compose down
```

To remove volumes as well:
```bash
docker-compose down -v
```
