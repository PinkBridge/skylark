# When Hub metadata/pull hangs, set MAVEN_BUILD_IMAGE / JRE_IMAGE (see docker/build-cn.env.example)
ARG MAVEN_BUILD_IMAGE=maven:3.9-eclipse-temurin-17
ARG JRE_IMAGE=eclipse-temurin:17-jre-alpine

FROM ${MAVEN_BUILD_IMAGE} AS builder
WORKDIR /build

COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests package

FROM ${JRE_IMAGE}
WORKDIR /app

COPY --from=builder /build/target/*.jar /app/app.jar

EXPOSE 80
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
