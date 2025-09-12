# Step 1: Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy everything and build with gradle or maven
COPY . .

RUN ./mvnw clean package -DskipTests

# Step 2: Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port (Spring will override via ENV)
EXPOSE 8080

# Run jar
ENTRYPOINT ["java", "-jar", "app.jar"]