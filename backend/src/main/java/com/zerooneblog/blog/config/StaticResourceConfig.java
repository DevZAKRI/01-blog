package com.zerooneblog.blog.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files under {project-root}/uploads/** at /uploads/**
        Path uploadDir = Path.of(System.getProperty("user.dir"), "uploads");
        String uploadPath = uploadDir.toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(uploadPath);
    }
}
