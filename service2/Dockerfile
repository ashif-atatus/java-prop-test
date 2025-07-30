# Build stage
FROM openjdk:21-jdk-slim AS builder

# Install curl for downloading dependencies
RUN apt-get update && apt-get install -y curl libstdc++6 && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better layer caching)
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./

# Make mvnw executable
RUN chmod +x mvnw

# Download Maven wrapper JAR if it doesn't exist
RUN if [ ! -f .mvn/wrapper/maven-wrapper.jar ]; then \
    curl -o .mvn/wrapper/maven-wrapper.jar https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar; \
    fi

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Download Atatus Java agent in build stage
RUN curl -o atatus-java-agent.jar https://atatus-artifacts.s3.amazonaws.com/atatus-java/downloads/latest/atatus-java-agent.jar

# Runtime stage
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/service2-0.0.1-SNAPSHOT.jar app.jar

# Copy Atatus Java agent from build stage
COPY --from=builder /app/atatus-java-agent.jar atatus-java-agent.jar

# Expose port
EXPOSE 3502

# Run the application with Atatus Java agent
CMD [ \
  "java", \
  "-javaagent:atatus-java-agent.jar", \
  "-jar", "app.jar" \
]
