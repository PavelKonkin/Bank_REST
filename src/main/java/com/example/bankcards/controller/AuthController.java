package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST контроллер для управления операциями аутентификации.
 * <p>
 * Предоставляет конечные точки (endpoints) для пользователей,
 * позволяющие им войти в систему и получить JWT-токен,
 * необходимый для доступа к защищенным ресурсам API.
 * </p>
 * Базовый путь для всех эндпоинтов в этом контроллере: {@code /api/v1/auth}.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Аутентификация", description = "API для получения JWT-токена")
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Конструктор для {@code AuthController}.
     * <p>
     * Внедряет зависимость {@link AuthenticationService}, которая отвечает
     * за бизнес-логику аутентификации пользователей.
     * </p>
     * @param authenticationService Сервис, предоставляющий функциональность аутентификации.
     */
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Обрабатывает запрос на аутентификацию пользователя.
     * <p>
     * Принимает учетные данные пользователя (имя пользователя и пароль) в теле запроса.
     * В случае успешной аутентификации, возвращает объект {@link AuthResponse},
     * содержащий JWT-токен. Этот токен должен быть включен в заголовок {@code Authorization}
     * (обычно как {@code Bearer <token>}) для доступа к защищенным ресурсам API.
     * </p>
     * <p>
     * Использует {@link jakarta.validation.Valid} для автоматической валидации входящего {@link AuthRequest}.
     * </p>
     *
     * @param request Объект {@link AuthRequest}, содержащий имя пользователя и пароль для аутентификации.
     *                Ожидается в теле запроса в формате JSON.
     * @return {@link AuthResponse} объект, содержащий сгенерированный JWT-токен.
     * @see AuthRequest
     * @see AuthResponse
     * @see AuthenticationService#login(AuthRequest)
     */
    @Operation(
            summary = "Аутентификация и получение JWT-токена",
            description = "Обменивает имя пользователя и пароль на JWT-токен для последующих запросов к защищенным эндпоинтам."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация, токен получен",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)) }
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверные учетные данные (Unauthorized)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный формат запроса (Bad Request)",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public AuthResponse login(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Учетные данные пользователя для входа",
            required = true,
            content = @Content(schema = @Schema(implementation = AuthRequest.class)))
            @Valid @RequestBody AuthRequest request) {
        return authenticationService.login(request);
    }
}
