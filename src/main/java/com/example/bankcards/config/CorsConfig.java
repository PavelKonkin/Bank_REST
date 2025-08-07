package com.example.bankcards.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Глобальная конфигурация CORS для всего приложения.
 * Позволяет фронтенду, адрес которого указан в свойствах приложения,
 * безопасно взаимодействовать с API.
 */
@Configuration
public class CorsConfig {

    /**
     * Считываем разрешенный источник (URL фронтенда) из application.properties.
     * Это значение, в свою очередь, может быть установлено через переменную окружения.
     */
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * @return бин WebMvcConfigurer с конфигураций CORS
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * добавляет маппинг CORS
             * @param registry список зарегестрированных правил CORS
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}