import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.concurrent.TimeUnit

plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.asciidoctor.jvm.convert") version "4.0.5"
}

group = "com.b216"
version = (findProperty("appVersion") as String?) ?: "0.0.1-SNAPSHOT"
description = "Backend service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
    }
}

extra["snippetsDir"] = file("build/generated-snippets")

dependencies {
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-geojson:7.9.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("tools.jackson.core:jackson-databind")
    implementation("org.springframework.session:spring-session-jdbc")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")
    implementation("org.hibernate.orm:hibernate-spatial")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val openApiOutputDir = layout.buildDirectory.dir("openapi")
val openApiPort = providers.gradleProperty("openApiPort").orElse("18081")
val openApiStartupTimeoutSeconds = providers.gradleProperty("openApiStartupTimeoutSeconds").orElse("90")

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    outputs.dir(project.extra["snippetsDir"]!!)
}

tasks.asciidoctor {
    inputs.dir(project.extra["snippetsDir"]!!)
    dependsOn(tasks.test)
}

tasks.register("exportOpenApi") {
    group = "documentation"
    description = "Starts the app in development profile and exports OpenAPI JSON and YAML"
    dependsOn(tasks.named("classes"))

    doLast {
        val outputPath = openApiOutputDir.get().asFile.toPath()
        Files.createDirectories(outputPath)

        val serverPort = openApiPort.get()
        val startupTimeoutSeconds = openApiStartupTimeoutSeconds.get().toLong()
        val javaExecutable = javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(25))
        }.get().executablePath.asFile.absolutePath

        val runtimeClasspath = sourceSets.main.get().runtimeClasspath.asPath
        val appProcess = ProcessBuilder(
            javaExecutable,
            "-Dspring.profiles.active=development",
            "-Dserver.port=$serverPort",
            "-cp",
            runtimeClasspath,
            "com.b216.umrs.UmrsBackApplication"
        )
            .redirectErrorStream(true)
            .start()

        val httpClient = HttpClient.newHttpClient()
        val jsonUri = URI.create("http://localhost:$serverPort/v3/api-docs/v1")
        val yamlUri = URI.create("http://localhost:$serverPort/v3/api-docs.yaml?group=v1")
        val jsonPath = outputPath.resolve("openapi.json")
        val yamlPath = outputPath.resolve("openapi.yaml")

        try {
            val startupDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(startupTimeoutSeconds)
            var isReady = false
            while (System.nanoTime() < startupDeadline) {
                try {
                    val readinessResponse = httpClient.send(
                        HttpRequest.newBuilder(jsonUri).GET().build(),
                        HttpResponse.BodyHandlers.ofString()
                    )
                    if (readinessResponse.statusCode() == 200) {
                        isReady = true
                        break
                    }
                } catch (_: Exception) {
                    // The app is still booting; continue polling until timeout.
                }
                Thread.sleep(1_000)
            }

            if (!isReady) {
                throw GradleException("OpenAPI endpoint did not become available within $startupTimeoutSeconds seconds")
            }

            val jsonResponse = httpClient.send(
                HttpRequest.newBuilder(jsonUri).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            )
            if (jsonResponse.statusCode() != 200) {
                throw GradleException("Failed to export OpenAPI JSON: HTTP ${jsonResponse.statusCode()}")
            }

            val yamlResponse = httpClient.send(
                HttpRequest.newBuilder(yamlUri).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            )
            if (yamlResponse.statusCode() != 200) {
                throw GradleException("Failed to export OpenAPI YAML: HTTP ${yamlResponse.statusCode()}")
            }

            Files.writeString(jsonPath, jsonResponse.body(), StandardCharsets.UTF_8)
            Files.writeString(yamlPath, yamlResponse.body(), StandardCharsets.UTF_8)
            logger.lifecycle("OpenAPI specs exported: {} and {}", jsonPath, yamlPath)
        } finally {
            appProcess.destroy()
            if (!appProcess.waitFor(10, TimeUnit.SECONDS)) {
                appProcess.destroyForcibly()
                appProcess.waitFor(5, TimeUnit.SECONDS)
            }
        }
    }
}
