# When Hub metadata/pull hangs, set MAVEN_BUILD_IMAGE / JRE_IMAGE (see docker/build-cn.env.example)
ARG MAVEN_BUILD_IMAGE=maven:3.9-eclipse-temurin-17
ARG JRE_IMAGE=eclipse-temurin:17-jre-alpine

FROM ${MAVEN_BUILD_IMAGE} AS builder
WORKDIR /build

ARG SERVICE_MODULE=business-service

COPY service/pom.xml service/pom.xml
COPY service ./service
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests -f service/pom.xml -pl ${SERVICE_MODULE} -am package

FROM ${JRE_IMAGE}
WORKDIR /app

ARG SERVICE_MODULE=business-service
COPY --from=builder /build/service/${SERVICE_MODULE}/target/*.jar /app/app.jar

ENV SERVER_PORT=80
EXPOSE 80
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
