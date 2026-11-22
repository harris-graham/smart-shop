# syntax=docker/dockerfile:1

# ---- Build stage -----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache dependencies separately from sources.
COPY pom.xml ./
RUN mvn -B -ntp -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests package

# ---- Runtime stage ---------------------------------------------------------
FROM eclipse-temurin:24-jre-alpine

RUN addgroup -S smartshop && adduser -S smartshop -G smartshop

WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar

USER smartshop
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/login || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
