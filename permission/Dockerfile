# BuildKit: caches ~/.m2 between builds (DOCKER_BUILDKIT=1, default on Docker Desktop)
# When Hub metadata/pull hangs, set in .env (see docker/build-cn.env.example):
#   MAVEN_BUILD_IMAGE=docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-17
#   JRE_IMAGE=docker.m.daocloud.io/library/eclipse-temurin:17-jre-alpine
ARG MAVEN_BUILD_IMAGE=maven:3.9-eclipse-temurin-17
ARG JRE_IMAGE=eclipse-temurin:17-jre-alpine

FROM ${MAVEN_BUILD_IMAGE} AS builder
WORKDIR /build

COPY service ./service
COPY permission ./permission
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests -f service/pom.xml -pl skylark-web-exception-spring-boot-starter -am install

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests -f permission/pom.xml package

FROM ${JRE_IMAGE}
WORKDIR /app

COPY --from=builder /build/permission/target/*.jar /app/app.jar

EXPOSE 80
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
