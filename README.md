# Java Spring Boot Microservices with Kafka, Docker Compose & Atatus Monitoring

This project demonstrates a modern microservices architecture with two Spring Boot services that communicate via HTTP and Apache Kafka, featuring comprehensive APM monitoring with Atatus and optimized multi-stage Docker builds.

- **Service 1** (Port 3501): Producer service with HTTP client capabilities and Kafka publishing
- **Service 2** (Port 3502): Consumer service with HTTP capabilities and Kafka consumption from "JPT" topic
- **Apache Kafka** (Port 9092): Message broker for asynchronous communication

## Package Structure
- Services use the `com.jpt` package naming convention
- **Service 1**: `com.jpt.service1` - Contains controller, Kafka producer, and HTTP client logic
- **Service 2**: `com.jpt.service2` - Contains controller, Kafka consumer, and HTTP client logic

## Features

### Service Endpoints
Each service exposes the following REST endpoints:
- `/health` - Returns comprehensive service information with environment details
- `/data` - Returns random data with timestamps and service metadata
- `/call` - Cross-service HTTP communication (Service 1 ↔ Service 2)
- `/produce-kafka-message` - (Service 1 only) Sends messages to Kafka "JPT" topic

### Messaging & Communication
- **Apache Kafka Integration** - Service 1 produces to "JPT" topic, Service 2 consumes from it
- **HTTP REST APIs** - Synchronous service-to-service communication
- **Fixed Topic Messaging** - Single "JPT" topic for producer-consumer pattern
- **JSON Message Processing** - Service 2 parses JSON messages with acknowledgment

### Monitoring & Observability
- **Atatus APM Integration** - Full application performance monitoring
- **Distributed Tracing** - End-to-end request tracking across services and Kafka
- **JVM Metrics** - Memory, GC, and thread monitoring
- **Error Tracking** - Exception monitoring with detailed stack traces
- **Kafka Monitoring** - Message flow and consumer lag tracking

### Infrastructure & DevOps
- **Multi-stage Docker builds** - Optimized image sizes with automatic dependency downloads
- **JDK 21** - Latest Java LTS version with performance improvements
- **KRaft Kafka** - Modern Kafka without Zookeeper dependency
- **Environment-based configuration** - Flexible deployment with separated configs
- **Auto-downloaded dependencies** - Maven wrapper and Atatus agent downloaded during build

## Prerequisites

- Docker & Docker Compose
- Internet connection (for automatic dependency downloads)

## Setup Instructions

### 1. Automatic Atatus Agent Setup
The Dockerfiles automatically download the Atatus Java agent during the build process. No manual download required!

The multi-stage build process:
1. **Build Stage**: Downloads Atatus agent from official S3 repository
2. **Runtime Stage**: Copies the agent to the final container image

```dockerfile
# Build stage automatically downloads the agent
RUN curl -o atatus-java-agent.jar https://atatus-artifacts.s3.amazonaws.com/atatus-java/downloads/latest/atatus-java-agent.jar

# Runtime stage includes the agent
COPY --from=builder /app/atatus-java-agent.jar atatus-java-agent.jar
```

### 2. Environment Configuration
The project uses environment files for APM monitoring configuration:

- `.env.atatus` - APM monitoring configuration

All Kafka settings are configured directly in `docker-compose.yml`.

### Environment Files Overview

#### Atatus Configuration (`.env.atatus`)
```bash
ATATUS_LICENSE_KEY=lic_apm_041b44a56fa54faaba026283c2b8970e
ATATUS_ENABLE=true
ATATUS_ENVIRONMENT=Production
ATATUS_LOG_LEVEL=info
ATATUS_TRACING=true
ATATUS_LOG_BODY=all
ATATUS_NOTIFY_HOST=https://demo.atatus.com
ATATUS_ANALYTICS=true
ATATUS_ANALYTICS_CAPTURE_OUTGOING=true
ATATUS_DEBUG=true
```

#### Service Configuration (Docker Compose)
Additional environment variables are configured in `docker-compose.yml`:

| Variable | Description | Service 1 | Service 2 |
|----------|-------------|-----------|-----------|
| `PORT` | Service port | `3501` | `3502` |
| `SERVICE2_URL` | Target URL for Service 1 | `http://service2:3502` | - |
| `SERVICE_1_URL` | Target URL for Service 2 | - | `http://service1:3501` |
| `ATATUS_APP_NAME` | Application name in Atatus | `JPT-Service-1` | `JPT-Service-2` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka connection string | `kafka:9092` | `kafka:9092` |

#### Kafka Environment Variables (Docker Compose)
All Kafka configuration is defined directly in `docker-compose.yml`:

| Variable | Value | Description |
|----------|-------|-------------|
| `KAFKA_NODE_ID` | `1` | Node identifier in KRaft cluster |
| `KAFKA_PROCESS_ROLES` | `broker,controller` | Kafka process roles |
| `KAFKA_CONTROLLER_LISTENER_NAMES` | `CONTROLLER` | Controller listener name |
| `KAFKA_LISTENER_SECURITY_PROTOCOL_MAP` | `CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT` | Security protocol mapping |
| `KAFKA_CONTROLLER_QUORUM_VOTERS` | `1@kafka:9093` | Controller quorum voters |
| `KAFKA_LISTENERS` | `PLAINTEXT://0.0.0.0:9092,CONTROLLER://kafka:9093` | Kafka listeners |
| `KAFKA_ADVERTISED_LISTENERS` | `PLAINTEXT://kafka:9092` | Advertised listeners |
| `KAFKA_INTER_BROKER_LISTENER_NAME` | `PLAINTEXT` | Inter-broker listener |
| `KAFKA_LOG_DIRS` | `/var/lib/kafka/data` | Log directory |
| `CLUSTER_ID` | `aDNkoppCRZmcMOQLUghOCA` | Static cluster identifier |

## Running the Services

### Quick Start
```bash
# Build and start all services (includes Kafka broker)
docker-compose up --build

# Run in detached mode
docker-compose up --build -d
```

### The services will be available at:
- **Service 1**: http://localhost:3501
- **Service 2**: http://localhost:3502  
- **Kafka Broker**: kafka:9092 (internal Docker network)
- **Atatus Dashboard**: https://demo.atatus.com (with your credentials)

### Build Process
The Docker build automatically:
1. Downloads Maven wrapper (if needed)
2. Downloads all dependencies
3. Downloads latest Atatus Java agent
4. Compiles and packages the Spring Boot applications
5. Creates optimized runtime containers with JDK 21

## Testing the Endpoints

### Service 1 Endpoints:
```bash
# Health check - shows service info and environment details
curl http://localhost:3501/health

# Get random data from Service 1
curl http://localhost:3501/data

# Call Service 2 from Service 1 (creates distributed trace)
curl http://localhost:3501/call

# Send message to Kafka topic (Service 1 only)
curl -X POST http://localhost:3501/produce-kafka-message \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello Kafka!", "type": "test"}'
```

### Service 2 Endpoints:
```bash
# Health check - shows service info and environment details  
curl http://localhost:3502/health

# Get random data from Service 2
curl http://localhost:3502/data

# Call Service 1 from Service 2 (creates distributed trace)
curl http://localhost:3502/call
```

### End-to-End Testing
```bash
# Test the complete flow: HTTP + Kafka + Distributed tracing
curl http://localhost:3501/call && \
curl -X POST http://localhost:3501/produce-kafka-message \
  -H "Content-Type: application/json" \
  -d '{"message": "End-to-end test", "timestamp": "'$(date)'"}'

# Monitor Service 2 logs to see Kafka message consumption
docker-compose logs -f service2
```

## Monitoring & Debugging

### Atatus Dashboard
1. **Login** to https://demo.atatus.com with your credentials
2. **View Applications**: You'll see `JPT-Service-1` and `JPT-Service-2`
3. **Distributed Traces**: Monitor `/call` endpoints for cross-service HTTP traces
4. **Kafka Traces**: View message publishing and consumption patterns
5. **Performance Metrics**: Analyze response times, throughput, and error rates
6. **JVM Monitoring**: Track memory usage, garbage collection, and thread performance

### Container Logs
```bash
# View all services including Kafka
docker-compose logs

# View specific service
docker-compose logs service1
docker-compose logs service2
docker-compose logs kafka

# Follow logs in real-time
docker-compose logs -f

# Filter for specific patterns
docker-compose logs | grep -i "error\|exception\|atatus"
```

### Verify Atatus Integration
Check container logs for Atatus initialization messages:
```bash
# Look for Atatus startup messages
docker-compose logs service1 | grep -i atatus
docker-compose logs service2 | grep -i atatus

# Verify Kafka connectivity
docker-compose logs | grep -i kafka
```

### Kafka Monitoring
```bash
# Check Kafka container status
docker-compose ps kafka

# View Kafka startup logs
docker-compose logs kafka | head -20

# View Kafka logs for topic creation and message handling
docker-compose logs kafka | grep -i topic

# Monitor Kafka internals (from inside Kafka container)
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

## Architecture

### Technology Stack
- **Java 21** - Latest LTS Java version with enhanced performance
- **Spring Boot 3.2.0** - Modern Spring framework with native image support
- **Apache Kafka 7.6.0** - High-throughput distributed streaming platform (KRaft mode)
- **Maven** - Dependency management with automatic wrapper download
- **Docker Multi-stage** - Optimized container builds with automated dependency management
- **Atatus APM** - Comprehensive application performance monitoring with distributed tracing

### Service Communication Patterns
- **Synchronous HTTP**: RESTful APIs for immediate request-response patterns
- **Asynchronous Messaging**: Kafka for decoupled, scalable communication
- **Service Discovery**: Docker Compose networking with hostname resolution
- **Load Balancing**: Ready for horizontal scaling with container orchestration

### Kafka Architecture (KRaft Mode)
```
Kafka Cluster (Single Node):
├── Controller Role: Topic/partition management
├── Broker Role: Message storage and delivery  
├── No Zookeeper: Simplified architecture
├── Static Cluster ID: aDNkoppCRZmcMOQLUghOCA
└── "JPT" Topic: Single topic for producer-consumer communication
```

### Docker Architecture
```
Build Stage (openjdk:21-jdk-slim):
├── Maven wrapper auto-download
├── Dependency resolution and download
├── Atatus agent download (latest version)
├── Source compilation and packaging
└── JAR file creation

Runtime Stage (openjdk:21-jdk-slim):
├── Atatus Java agent integration
├── Optimized JAR file
├── Minimal runtime environment
└── JVM performance tuning
```

### Network Architecture
```
Docker Network (app-network):
├── service1:3501 → HTTP + Kafka Producer
├── service2:3502 → HTTP + Kafka Consumer  
├── kafka:9092 → Message Broker
└── Cross-service communication via hostnames
```

## Troubleshooting

### Common Issues

1. **Services won't start**:
   ```bash
   # Check if ports are available
   netstat -tlnp | grep -E '3501|3502|9092'
   
   # Clean rebuild with no cache
   docker-compose down
   docker-compose up --build --no-cache
   ```

2. **Kafka connectivity issues**:
   ```bash
   # Verify Kafka is running
   docker-compose ps kafka
   
   # Check Kafka logs
   docker-compose logs kafka
   
   # Test Kafka from inside container
   docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
   ```

3. **Atatus agent not working**:
   ```bash
   # Verify agent download in build logs
   docker-compose up --build | grep -i atatus
   
   # Check if agent file exists in container
   docker-compose exec service1 ls -la atatus-java-agent.jar
   
   # Verify environment variables
   docker-compose exec service1 env | grep ATATUS
   ```

4. **Service communication errors**:
   ```bash
   # Check Docker network
   docker network ls
   docker network inspect java-propagation-test_default
   
   # Test inter-service connectivity
   docker-compose exec service1 curl http://service2:3502/health
   ```

5. **Memory or performance issues**:
   ```bash
   # Monitor container resource usage
   docker stats
   
   # Check JVM memory settings
   docker-compose logs service1 | grep -i memory
   ```

### Debug Mode
Enable additional debugging by modifying `.env.atatus`:
```bash
# Enhanced debugging
ATATUS_DEBUG=true
ATATUS_LOG_LEVEL=debug
ATATUS_LOG_BODY=all
```

### Performance Tuning
For production deployment, consider these JVM optimizations in Dockerfile:
```dockerfile
CMD [ \
  "java", \
  "-javaagent:atatus-java-agent.jar", \
  "-Xms512m", "-Xmx1g", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-jar", "app.jar" \
]
```

## Stopping the Services

```bash
# Stop all services gracefully
docker-compose down

# Stop and remove volumes (includes Kafka data)
docker-compose down -v

# Stop and remove everything (images, containers, volumes)
docker-compose down --rmi all -v

# Force stop if services are unresponsive
docker-compose kill && docker-compose down
```

## Development Notes

### Container Optimization
- **Multi-stage builds** reduce final image size by ~70%
- **Layer caching** optimizes build times for dependency downloads
- **JDK 21** provides enhanced performance and reduced memory footprint
- **Automatic dependency management** eliminates manual setup requirements

### Monitoring Integration
- **Atatus** automatically instruments HTTP requests, database calls, and JVM metrics
- **Distributed tracing** tracks requests across service boundaries and Kafka messages
- **Real-time alerting** can be configured for error rates and performance thresholds
- **Custom metrics** can be added using Atatus SDK annotations

### Kafka Configuration
- **KRaft mode** eliminates Zookeeper dependency for simplified deployment
- **Static cluster ID** configured as `aDNkoppCRZmcMOQLUghOCA` for consistent setup
- **Single topic setup** using "JPT" topic for producer-consumer pattern
- **Single broker setup** suitable for development; easily scalable for production
- **Persistent volumes** ensure message durability across container restarts

### Production Considerations
- Replace single Kafka broker with cluster setup
- Implement proper security (SSL/SASL) for Kafka and service communication
- Add health checks and liveness probes for Kubernetes deployment
- Configure appropriate resource limits and requests
- Set up log aggregation (ELK stack) alongside Atatus monitoring
- Implement circuit breakers for resilient service communication

### Testing Strategy
- **Unit tests**: Mock external dependencies including Kafka
- **Integration tests**: Use Testcontainers for real Kafka testing
- **End-to-end tests**: Validate complete request flows and tracing
- **Performance tests**: Load test with realistic message volumes

### Scaling Guidelines
```yaml
# Example horizontal scaling with Docker Compose
services:
  service1:
    deploy:
      replicas: 3
  kafka:
    deploy:
      replicas: 3  # Kafka cluster setup
```

## Project Structure

```
java-propagation-test/
├── service1/
│   ├── src/main/java/com/jpt/service1/
│   │   ├── Service1Application.java     # Main Spring Boot application
│   │   ├── Service1Controller.java      # REST endpoints and HTTP client
│   │   └── KafkaProducerService.java    # Kafka message publishing
│   ├── pom.xml                          # Maven dependencies (Spring Boot, Kafka)
│   └── Dockerfile                       # Multi-stage build with Atatus agent
├── service2/
│   ├── src/main/java/com/jpt/service2/
│   │   ├── Service2Application.java     # Main Spring Boot application  
│   │   ├── Service2Controller.java      # REST endpoints and HTTP client
│   │   └── KafkaConsumerService.java    # Kafka message consumption
│   ├── pom.xml                          # Maven dependencies (Spring Boot, Kafka)
│   └── Dockerfile                       # Multi-stage build with Atatus agent
├── docker-compose.yml                   # Service orchestration with Kafka configuration
├── .env.atatus                          # APM monitoring configuration
└── README.md                            # This documentation
```

### Key Files Description

- **Application Classes**: Standard Spring Boot main classes with `@SpringBootApplication`
- **Controller Classes**: REST endpoints (`/health`, `/data`, `/call`) with HTTP client logic
- **KafkaProducerService**: Service 1 only - publishes messages to "JPT" topic
- **KafkaConsumerService**: Service 2 only - consumes messages from "JPT" topic with JSON parsing
- **Dockerfiles**: Multi-stage builds that automatically download Atatus agent
- **Docker Compose**: Orchestrates all services with Kafka configuration and networking
- **Environment File**: APM monitoring configuration only (`.env.atatus`)
