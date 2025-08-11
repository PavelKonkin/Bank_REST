package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import com.example.bankcards.util.ValidRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Объект передачи данных (DTO) для создания нового пользователя в системе.
 * Этот класс используется для инкапсуляции информации, необходимой для регистрации
 * нового пользователя, включая его имя пользователя (логин), пароль и роль.
 * Включает аннотации для валидации данных и для генерации документации OpenAPI (Swagger).
 */
@Data
@Schema(description = "DTO для создания нового пользователя")
public class CreateUserDto {
    /**
     * Уникальный идентификатор пользователя.
     * Это поле используется только для чтения и игнорируется при создании нового пользователя,
     * так как ID обычно генерируется базой данных или системой.
     */
    @Schema(description = "Уникальный ID (игнорируется при создании)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    /**
     * Имя пользователя (логин), используемое для входа в систему.
     * Должно быть уникальным для каждого пользователя.
     * Не может быть пустым и должно содержать от 2 до 255 символов.
     */
    @Schema(description = "Имя пользователя (логин)", requiredMode = Schema.RequiredMode.REQUIRED, example = "new_user")
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 255, message = "Имя пользователя должно содержать от 2 до 255 символов")
    private String username;

    /**
     * Пароль пользователя.
     * Это поле предназначено только для записи и не должно возвращаться в ответах API.
     * Не может быть пустым и должно содержать от 2 до 255 символов.
     */
    @Schema(description = "Пароль пользователя",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            example = "Str0ngP@ssw0rd!")
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 2, max = 255, message = "Пароль должен содержать от 2 до 255 символов")
    private String password;

    /**
     * Роль пользователя в системе.
     * Определяет права доступа пользователя.
     * Допустимые значения ограничены перечислением {@link UserRole},
     * например, "ROLE_USER" или "ROLE_ADMIN".
     * Не может быть пустым и должно соответствовать одной из предопределенных ролей.
     */
    @Schema(description = "Роль пользователя. Допустимые значения: ROLE_USER, ROLE_ADMIN",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "ROLE_USER")
    @NotBlank(message = "Роль не может быть пустой")
    @ValidRole(enumClass = UserRole.class)
    private String role;
}
