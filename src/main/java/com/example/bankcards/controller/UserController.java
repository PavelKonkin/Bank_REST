package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Управление пользователями (Admin)", description = "API для создания, получения и удаления пользователей. Доступно только администраторам.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Получить список всех пользователей",
            description = "Возвращает полный список всех зарегистрированных пользователей.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = { @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))}),
            @ApiResponse(responseCode = "401", description = "Не авторизован", content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "Доступ запрещен (необходима роль ADMIN)", content = @Content)
    })
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.findAllUsers();
    }

    @Operation(summary = "Получить пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))}),
            @ApiResponse(responseCode = "401", description = "Не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public UserDto getUserById(
            @Parameter(description = "ID пользователя, которого нужно найти", required = true, example = "1")
            @PathVariable Long id) {
        return userService.findUserById(id);
    }

    @Operation(summary = "Создать нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан", content =  @Content),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового пользователя", required = true,
                    content = @Content(schema = @Schema(implementation = CreateUserDto.class)))
            @Valid @RequestBody CreateUserDto user) {
        userService.createUser(user);
    }

    @Operation(summary = "Удалить пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален", content = @Content),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @Parameter(description = "ID пользователя для удаления", required = true, example = "2")
            @PathVariable Long id) {
        userService.deleteUser(id);
    }
}
