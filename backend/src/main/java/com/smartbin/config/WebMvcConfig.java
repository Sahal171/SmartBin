package com.smartbin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public WebMvcConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = appProperties.getCors().getAllowedOrigins().toArray(new String[0]);
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition", "Content-Type", "Content-Length")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
