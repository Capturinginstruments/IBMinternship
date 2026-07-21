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
# Generate custom my.cnf to use writable directories with low-memory optimization
cat << EOF > /app/mysql/my.cnf
[mysqld]
datadir=/app/mysql/data
socket=/app/mysql/mysql.sock
pid-file=/app/mysql/mysql.pid
log-error=/app/mysql/mysql.err
tmpdir=/tmp
secure_file_priv=""
port=3306
bind-address=127.0.0.1
mysqlx=OFF
performance_schema=OFF
innodb_buffer_pool_size=16M
innodb_log_buffer_size=2M
key_buffer_size=8M
max_connections=10
EOF

# Initialize MySQL if the data directory is empty
if [ ! -d "/app/mysql/data/mysql" ]; then
    echo "Initializing MySQL data directory..."
    if ! /usr/sbin/mysqld --defaults-file=/app/mysql/my.cnf --initialize-insecure --user=$(id -u); then
        echo "ERROR: MySQL initialization failed!"
        cat /app/mysql/mysql.err
        exit 1
    fi
fi

echo "Starting MySQL..."
/usr/sbin/mysqld --defaults-file=/app/mysql/my.cnf --user=$(id -u) &

# Wait for MySQL to start up
echo "Waiting for MySQL to start..."
for i in {1..30}; do
    if /usr/bin/mysqladmin --socket=/app/mysql/mysql.sock ping >/dev/null 2>&1; then
        echo "MySQL is running!"
        break
    fi
    sleep 1
done

# Initialize database schema and seeds if not already done
if ! /usr/bin/mysql --socket=/app/mysql/mysql.sock -e "use farmer_assistant;" >/dev/null 2>&1; then
    echo "Creating database and user..."
    /usr/bin/mysql --socket=/app/mysql/mysql.sock -e "CREATE DATABASE farmer_assistant;"
    /usr/bin/mysql --socket=/app/mysql/mysql.sock -e "CREATE USER '\''farmer'\''@'\''localhost'\'' IDENTIFIED BY '\''farmer123'\'';"
    /usr/bin/mysql --socket=/app/mysql/mysql.sock -e "GRANT ALL PRIVILEGES ON farmer_assistant.* TO '\''farmer'\''@'\''localhost'\'';"
    /usr/bin/mysql --socket=/app/mysql/mysql.sock -e "FLUSH PRIVILEGES;"

    echo "Importing schema..."
    /usr/bin/mysql --socket=/app/mysql/mysql.sock farmer_assistant < /app/schema.sql
    echo "Importing seeds..."
    /usr/bin/mysql --socket=/app/mysql/mysql.sock farmer_assistant < /app/seed.sql
    /usr/bin/mysql --socket=/app/mysql/mysql.sock farmer_assistant < /app/seed_market_prices.sql
    echo "Database initialization complete!"
fi

echo "Starting Spring Boot application..."
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/farmer_assistant?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true"
export SPRING_DATASOURCE_USERNAME="farmer"
export SPRING_DATASOURCE_PASSWORD="farmer123"

exec java -Xms64m -Xmx200m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC -jar app.jar
' > /app/entrypoint.sh && chmod +x /app/entrypoint.sh

# Ensure the /app directory has correct permissions
RUN chmod -R 777 /app

EXPOSE 8080

ENTRYPOINT ["/app/entrypoint.sh"]
