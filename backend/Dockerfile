# ─── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.7-eclipse-temurin-21 AS builder
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:go-offline -q
COPY backend/src ./src
RUN mvn clean package -DskipTests -q

# ─── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM ubuntu:22.04

# Avoid prompt during installations
ENV DEBIAN_FRONTEND=noninteractive

# Install Java 21, MySQL Server, curl, and clean up
RUN apt-get update && apt-get install -y \
    openjdk-21-jre-headless \
    mysql-server \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the built jar from Stage 1
COPY --from=builder /app/target/farmer-assistant-*.jar app.jar

# Copy database initialization files (context is root of repo)
COPY database/schema.sql /app/schema.sql
COPY database/seed.sql /app/seed.sql
COPY database/seed_market_prices.sql /app/seed_market_prices.sql

# Create custom startup script
RUN echo '#!/bin/bash\n\
echo "Starting MySQL..."\n\
usermod -d /var/lib/mysql/ mysql\n\
find /var/lib/mysql -type f -exec touch {} + \n\
service mysql start\n\
\n\
echo "Initializing database schema..."\n\
mysql -e "CREATE DATABASE IF NOT EXISTS farmer_assistant;"\n\
mysql -e "CREATE USER IF NOT EXISTS '\''farmer'\''@'\''%'\'' IDENTIFIED BY '\''farmer123'\'';"\n\
mysql -e "GRANT ALL PRIVILEGES ON farmer_assistant.* TO '\''farmer'\''@'\''%'\'';"\n\
mysql -e "FLUSH PRIVILEGES;"\n\
\n\
echo "Applying schemas and seeds..."\n\
mysql farmer_assistant < /app/schema.sql\n\
mysql farmer_assistant < /app/seed.sql\n\
mysql farmer_assistant < /app/seed_market_prices.sql\n\
\n\
echo "Starting Spring Boot application..."\n\
java -jar app.jar\n\
' > /app/entrypoint.sh && chmod +x /app/entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/app/entrypoint.sh"]
