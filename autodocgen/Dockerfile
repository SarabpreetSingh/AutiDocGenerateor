# ==============================
# 1. Build Stage
# ==============================
FROM openjdk:24-jdk AS builder

# Set the working directory
WORKDIR /app

# Copy Maven wrapper and config files
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Ensure Maven wrapper is executable
RUN chmod +x mvnw

# Preload dependencies (for better Docker layer caching)
RUN ./mvnw dependency:go-offline -B

# Copy the rest of the source code
COPY src src

# Build the Spring Boot fat (shadow) JAR
RUN ./mvnw clean package -DskipTests

# ==============================
# 2. Runtime Stage
# ==============================
FROM openjdk:24-jdk-slim

# Set working directory
WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=builder /app/target/*.jar app.jar

# Expose Spring Boot's default port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]