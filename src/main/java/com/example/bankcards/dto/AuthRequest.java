package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Представляет собой объект передачи данных (DTO) для запросов аутентификации.
 * <p>
 * Этот рекорд используется для инкапсуляции учетных данных пользователя (имя пользователя и пароль)
 * при попытке входа в систему. Он является неизменяемым.
 * </p>
 * <p>
 * Поля {@code username} и {@code password} содержат ограничения валидации,
 * обеспечивающие, что они не являются пустыми и имеют допустимую длину
 * </p>
 */
@Schema(description = "Запрос на аутентификацию")
public record AuthRequest(
        @Schema(description = "Имя пользователя", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
        @NotBlank(message = "Имя пользователя не может быть пустым")
        @Size(min = 2, max = 255, message = "Имя пользователя должно содержать от 2 до 255 символов")
        String username,

        @Schema(description = "Пароль", requiredMode = Schema.RequiredMode.REQUIRED, example = "password")
        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 2, max = 255, message = "Пароль должен содержать от 2 до 255 символов")
        String password
) {}
