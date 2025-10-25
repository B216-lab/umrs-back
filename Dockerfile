FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace/app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

COPY src src

RUN chmod +x gradlew \
    && ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre-jammy
ENV SPRING_PROFILES_ACTIVE=prod
WORKDIR /app

COPY --from=build /workspace/app/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]


