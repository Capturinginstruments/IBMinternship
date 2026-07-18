# ─── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.7-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# ─── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S farmer && adduser -S farmer -G farmer
COPY --from=builder /app/target/farmer-assistant-*.jar app.jar
RUN chown farmer:farmer app.jar
USER farmer
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "echo 'Checking database host: '$DB_HOST; until nc -z -w5 $DB_HOST 3306; do echo 'Database not ready, sleeping 3 seconds...'; sleep 3; done; echo 'Database is ready! Starting backend...'; java $JAVA_OPTS -jar app.jar"]
