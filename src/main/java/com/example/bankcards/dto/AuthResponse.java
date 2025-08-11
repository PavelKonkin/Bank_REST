package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Представляет собой объект передачи данных (DTO) для ответов аутентификации.
 * <p>
 * Этот рекорд используется для передачи сгенерированного JWT-токена клиенту
 * после успешной аутентификации. Он является неизменяемым.
 * </p>
 */
@Schema(description = "Ответ с JWT-токеном")
public record AuthResponse(
        @Schema(description = "Сгенерированный JWT-токен")
        String token
) {}
