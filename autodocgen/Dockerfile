# ==============================
# 1. Build Stage
# ==============================
FROM eclipse-temurin:21-jdk AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy Maven wrapper and configuration
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Make mvnw executable
RUN chmod +x mvnw

# Preload dependencies for better caching
RUN ./mvnw dependency:go-offline -B

# Copy the rest of the source code
COPY src src

# Build the Spring Boot fat (shadow) JAR
RUN ./mvnw clean package -DskipTests

# ==============================
# 2. Runtime Stage
# ==============================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose Spring Boot’s default port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]