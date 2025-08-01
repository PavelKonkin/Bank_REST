package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с JWT-токеном")
public record AuthResponse(
        @Schema(description = "Сгенерированный JWT-токен")
        String token
) {}
