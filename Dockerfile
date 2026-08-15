# ==========================================
# Stage 1: Build & Package with Maven
# ==========================================
FROM maven:3.9-eclipse-temurin-8-alpine AS builder

WORKDIR /build

# Copy Maven POM and Wrapper first to leverage Docker layer caching
COPY pom.xml .
COPY checkstyle.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies in offline mode
RUN ./mvnw dependency:go-offline -B || true

# Copy source code
COPY src src

# Build and package the executable JAR
RUN ./mvnw clean package -DskipTests

# ==========================================
# Stage 2: Minimal Runtime with Alpine JRE 8
# ==========================================
FROM eclipse-temurin:8-jre-alpine

WORKDIR /app

# Create directory for data storage and reports
RUN mkdir -p data data/backup reports

# Copy the built JAR from builder stage
COPY --from=builder /build/target/movie-streaming-management-system-1.0.0-SNAPSHOT.jar app.jar

# Run interactive CLI by default
ENTRYPOINT ["java", "-jar", "app.jar"]
