package com.example.bankcards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс для OpenAPI (Swagger) документации.
 * <p>
 * Определяет пользовательскую спецификацию OpenAPI для API управления банковскими картами и транзакциями,
 * включая информацию об API и схемы безопасности для аутентификации с использованием JWT bearer токенов.
 * </p>
 */
@Configuration
public class OpenApiConfig {
    /**
     * Конфигурирует и предоставляет пользовательский бин спецификации OpenAPI (Swagger).
     * <p>
     * Этот метод устанавливает основную информацию об API (заголовок, описание, версию)
     * и определяет схему безопасности для аутентификации с использованием JWT bearer токенов.
     * Это гарантирует, что все конечные точки, требующие аутентификации, будут правильно
     * задокументированы в пользовательском интерфейсе Swagger.
     * </p>
     *
     * @return Экземпляр {@link io.swagger.v3.oas.models.OpenAPI}, настроенный для данного приложения.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new     SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .info(new Info().title("Bank Card & Transaction API")
                        .description("API для управления пользователями, банковскими картами и проведения транзакций.")
                        .version("v1.1.0"));
    }
}
