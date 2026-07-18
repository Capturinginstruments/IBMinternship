FROM mysql:8.0

# Copy initialization scripts
COPY schema.sql /docker-entrypoint-initdb.d/01-schema.sql
COPY seed.sql /docker-entrypoint-initdb.d/02-seed.sql
COPY seed_market_prices.sql /docker-entrypoint-initdb.d/03-seed-market-prices.sql

# Expose MySQL port
EXPOSE 3306
