FROM mysql:8.0-debian

# Install python3 to run a dummy HTTP server for Render's health checks
RUN apt-get update && apt-get install -y python3 && rm -rf /var/lib/apt/lists/*

# Copy initialization scripts
COPY schema.sql /docker-entrypoint-initdb.d/01-schema.sql
COPY seed.sql /docker-entrypoint-initdb.d/02-seed.sql
COPY seed_market_prices.sql /docker-entrypoint-initdb.d/03-seed-market-prices.sql

# Copy custom entrypoint script
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Expose MySQL and dynamic HTTP port
EXPOSE 3306

ENTRYPOINT ["entrypoint.sh"]
