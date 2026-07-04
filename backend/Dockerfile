# Backend Dockerfile - Multi-stage build
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
# Download dependencies first (cache layer)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install mysql client for backup/restore feature
RUN apk add --no-cache mysql-client tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

COPY --from=builder /build/target/relic-admin-1.0.0.jar app.jar

# Create backup directory
RUN mkdir -p /data/backups

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m -Duser.timezone=Asia/Shanghai"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=docker"]
