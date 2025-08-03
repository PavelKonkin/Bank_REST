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

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Аутентификация", description = "API для получения JWT-токена")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

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
