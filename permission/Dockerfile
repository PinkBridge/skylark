# syntax=docker/dockerfile:1
# BuildKit: caches ~/.m2 between builds (DOCKER_BUILDKIT=1, default on Docker Desktop)
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /build/target/*.jar /app/app.jar

EXPOSE 80
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
