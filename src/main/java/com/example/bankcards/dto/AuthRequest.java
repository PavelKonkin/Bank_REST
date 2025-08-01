package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на аутентификацию")
public record AuthRequest(
        @Schema(description = "Имя пользователя", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
        String username,

        @Schema(description = "Пароль", requiredMode = Schema.RequiredMode.REQUIRED, example = "password")
        String password
) {}
