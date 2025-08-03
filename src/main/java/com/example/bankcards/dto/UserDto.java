package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO с публичной информацией о пользователе (для ответа)")
public class UserDto {
    @Schema(description = "Уникальный идентификатор пользователя",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "101")
    private Long id;
    @Schema(description = "Имя пользователя (логин)",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "existing_user")
    private String username;
    @Schema(description = "Роль пользователя",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "ROLE_ADMIN")
    private String role;
}
