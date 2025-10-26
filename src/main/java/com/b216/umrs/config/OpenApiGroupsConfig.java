package com.b216.umrs.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация групп OpenAPI для версий v0 и v1.
 */
@Configuration
public class OpenApiGroupsConfig {

    @Bean
    public GroupedOpenApi apiV1() { // legacy
        return GroupedOpenApi.builder()
            .group("v1")
            .pathsToMatch("/api/v0/**")
            .build();
    }

    @Bean
    public GroupedOpenApi apiV2() {
        return GroupedOpenApi.builder()
            .group("v2")
            .pathsToMatch("/api/v1/**")
            .build();
    }
}
