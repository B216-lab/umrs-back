package com.b216.umrs.config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGroupsConfig {

    @Bean
    public GroupedOpenApi apiV1() {
        return GroupedOpenApi.builder()
            .group("v1")
            .pathsToMatch("/api/v1/**")
            .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                .title("UMRS API")
                .version("v1")
                .description("REST API for UMRS backend")))
            .build();
    }
}
