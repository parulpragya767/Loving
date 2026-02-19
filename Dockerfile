# ---------- Build Stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# Copy source
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests


# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Create non-root user
RUN useradd -ms /bin/bash appuser

# Create PostgreSQL cert directory for appuser
RUN mkdir -p /home/appuser/.postgresql

# Copy Supabase CA certificate
COPY certs/supabase-ca.crt /home/appuser/.postgresql/root.crt

# Set proper ownership and permissions (required by PostgreSQL)
RUN chown -R appuser:appuser /home/appuser/.postgresql && \
    chmod 700 /home/appuser/.postgresql && \
    chmod 600 /home/appuser/.postgresql/root.crt

# Copy jar
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]