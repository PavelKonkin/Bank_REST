package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import com.example.bankcards.util.ValidRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO для создания нового пользователя")
public class CreateUserDto {
     @Schema(description = "Уникальный ID (игнорируется при создании)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Имя пользователя (логин)", requiredMode = Schema.RequiredMode.REQUIRED, example = "new_user")
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 255, message = "Имя пользователя должно содержать от 2 до 255 символов")
    private String username;

    @Schema(description = "Пароль пользователя",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            example = "Str0ngP@ssw0rd!")
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 2, max = 255, message = "Пароль должен содержать от 2 до 255 символов")
    private String password;

    @Schema(description = "Роль пользователя. Допустимые значения: ROLE_USER, ROLE_ADMIN",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "ROLE_USER")
    @NotBlank(message = "Роль не может быть пустой")
    @ValidRole(enumClass = UserRole.class)
    private String role;
}
