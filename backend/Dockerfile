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

# Create custom non-root startup script for MySQL + Spring Boot
RUN echo '#!/bin/bash\n\
set -e\n\
\n\
echo "Setting up custom MySQL directories..."\n\
mkdir -p /app/mysql/data\n\
mkdir -p /app/mysql/run\n\
\n\
# Generate custom my.cnf to use writable directories\n\
cat << EOF > /app/mysql/my.cnf\n\
[mysqld]\n\
datadir=/app/mysql/data\n\
socket=/app/mysql/mysql.sock\n\
pid-file=/app/mysql/mysql.pid\n\
log-error=/app/mysql/mysql.err\n\
tmpdir=/tmp\n\
secure_file_priv=""\n\
port=3306\n\
bind-address=127.0.0.1\n\
mysqlx=OFF\n\
EOF\n\
\n\
# Initialize MySQL if the data directory is empty\n\
if [ ! -d "/app/mysql/data/mysql" ]; then\n\
    echo "Initializing MySQL data directory..."\n\
    /usr/sbin/mysqld --defaults-file=/app/mysql/my.cnf --no-defaults --initialize-insecure\n\
fi\n\
\n\
echo "Starting MySQL..."\n\
/usr/sbin/mysqld --defaults-file=/app/mysql/my.cnf --no-defaults &\n\
\n\
# Wait for MySQL to start up\n\
echo "Waiting for MySQL to start..."\n\
for i in {1..30}; do\n\
    if /usr/bin/mysqladmin --socket=/app/mysql/mysql.sock ping >/dev/null 2>&1; then\n\
        echo "MySQL is running!"\n\
        break\n\
    fi\n\
    sleep 1\n\
done\n\
\n\
# Initialize database schema and seeds if not already done\n\
if ! /usr/bin/mysql --socket=/app/mysql/mysql.sock -e "use farmer_assistant;" >/dev/null 2>&1; then\n\
    echo "Creating database and user..."\n\
    /usr/bin/mysql --socket=/app/mysql/mysql.sock -e "CREATE DATABASE farmer_assistant;"\n\
    /usr/bin/mysql --socket=/app/mysql/mysql.sock -e "CREATE USER '\''farmer'\''@'\''localhost'\'' IDENTIFIED BY '\''farmer123'\'';"\n\
    /usr/bin/mysql --socket=/app/mysql/mysql.sock -e "GRANT ALL PRIVILEGES ON farmer_assistant.* TO '\''farmer'\''@'\''localhost'\'';"\n\
    /usr/bin/mysql --socket=/app/mysql/mysql.sock -e "FLUSH PRIVILEGES;"\n\
\n\
    echo "Importing schema..."\n\
    /usr/bin/mysql --socket=/app/mysql/mysql.sock farmer_assistant < /app/schema.sql\n\
    echo "Importing seeds..."\n\
    /usr/bin/mysql --socket=/app/mysql/mysql.sock farmer_assistant < /app/seed.sql\n\
    /usr/bin/mysql --socket=/app/mysql/mysql.sock farmer_assistant < /app/seed_market_prices.sql\n\
    echo "Database initialization complete!"\n\
fi\n\
\n\
echo "Starting Spring Boot application..."\n\
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/farmer_assistant?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true"\n\
export SPRING_DATASOURCE_USERNAME="farmer"\n\
export SPRING_DATASOURCE_PASSWORD="farmer123"\n\
\n\
exec java -jar app.jar\n\
' > /app/entrypoint.sh && chmod +x /app/entrypoint.sh

# Ensure the /app directory has correct permissions
RUN chmod -R 777 /app

EXPOSE 8080

ENTRYPOINT ["/app/entrypoint.sh"]
