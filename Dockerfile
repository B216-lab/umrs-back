FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace/app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

COPY src src

RUN chmod +x gradlew \
    && ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre-jammy


LABEL org.opencontainers.image.title="UMRS Backend" \
    org.opencontainers.image.description="Backend service for UMRS" \
    org.opencontainers.image.version="0.0.0" \
    org.opencontainers.image.vendor="B216" \
    org.opencontainers.image.licenses="GPL-3.0" \
    org.opencontainers.image.authors="Kirill Zhilenkov & B216 Team" \
    org.opencontainers.image.source="https://github.com/B216-lab/umrs-back" \
    org.opencontainers.image.url="https://github.com/B216-lab/umrs-back" \
    maintainer="Kirill Zhilenkov & B216 Team"


ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8081
WORKDIR /app

COPY --from=build /workspace/app/build/libs/*.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]


