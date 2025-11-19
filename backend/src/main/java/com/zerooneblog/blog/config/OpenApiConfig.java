package com.zerooneblog.blog.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "org.springdoc.core.GroupedOpenApi")
public class OpenApiConfig {
    // Springdoc is optional in this project. If present, user can add beans here.
    @Bean
    public Object openApiPlaceholder() {
        return new Object();
    }
}
