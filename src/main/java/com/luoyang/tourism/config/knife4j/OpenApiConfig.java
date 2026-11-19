package com.luoyang.tourism.config.knife4j;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * OpenApiConfig 配置组件。
 * <p>负责基础设施、安全或 Web 相关配置。</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tourismOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("洛阳景点智能推荐管理系统 API")
                        .version("v1")
                        .description("Spring Boot 是唯一对外业务入口。所有 BIGINT ID 均以字符串返回。")
                        .contact(new Contact().name("luoyang-smart-tourism")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder().group("public")
                .pathsToMatch("/api/v1/public/**").build();
    }

    @Bean
    public GroupedOpenApi appApi() {
        return GroupedOpenApi.builder().group("app")
                .pathsToMatch("/api/v1/app/**").build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder().group("admin")
                .pathsToMatch("/api/v1/admin/**").build();
    }

    @Bean
    @Profile({"local", "test"})
    public GroupedOpenApi internalApi() {
        return GroupedOpenApi.builder().group("internal")
                .pathsToMatch("/internal/**").build();
    }
}
