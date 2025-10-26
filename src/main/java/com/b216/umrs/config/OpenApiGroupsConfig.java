package com.b216.umrs.config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация групп OpenAPI для legacy и v1.
 */
@Configuration
public class OpenApiGroupsConfig {

    @Bean
    public GroupedOpenApi apiV0() { // legacy
        return GroupedOpenApi.builder()
            .group("v0")
            .pathsToMatch("/api/v0/**")
            .addOpenApiCustomizer(openApi -> openApi.info(new Info().title("Legacy UMRS API").version("0")))
            .build();
    }

    @Bean
    public GroupedOpenApi apiV1() {
        return GroupedOpenApi.builder()
            .group("v1")
            .pathsToMatch("/api/v1/**")
            .addOpenApiCustomizer(openApi -> openApi.info(new Info().title("UMRS API v1").version("1")))
            .build();
    }
}
