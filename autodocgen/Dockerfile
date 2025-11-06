# ==============================
# 1. Build Stage
# ==============================
FROM eclipse-temurin:21-jdk AS builder

# Set the working directory inside the container
WORKDIR /app

# Install Graphviz (needed if PlantUML runs during build)
RUN apt-get update && apt-get install -y graphviz && rm -rf /var/lib/apt/lists/*

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

# Set working directory
WORKDIR /app

# Install Graphviz (needed at runtime for PlantUML)
RUN apt-get update && apt-get install -y graphviz && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose Spring Boot’s default port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
