package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
