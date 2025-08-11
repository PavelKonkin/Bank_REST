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

/**
 * Контроллер для управления пользователями в системе.
 * <p>
 * Предоставляет API эндпоинты для выполнения CRUD-операций над пользователями.
 * Все операции в этом контроллере требуют наличия у аутентифицированного пользователя роли 'ADMIN'.
 * Базовый путь для всех эндпоинтов: {@code /api/v1/admin/users}.
 * Требуется аутентификация по Bearer Token.
 * </p>
 *
 * @author Pavel Konkin
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Управление пользователями (Admin)", description = "API для создания, получения и удаления пользователей. Доступно только администраторам.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    /**
     * Конструктор для {@code UserController}.
     * <p>
     * Внедряет зависимость {@link UserService}, которая отвечает
     * за бизнес-логику, связанную с управлением пользователями.
     * </p>
     * @param userService Сервис, предоставляющий функциональность для работы с пользователями.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Возвращает список всех зарегистрированных пользователей системы.
     * <p>
     * Этот эндпоинт доступен только для пользователей с ролью {@code ADMIN}.
     * </p>
     *
     * @return Список объектов {@link UserDto}, представляющих всех пользователей.
     *         Возвращает пустой список, если пользователей нет.
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не имеет роли ADMIN.
     * @see com.example.bankcards.service.UserService#findAllUsers()
     */
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

/**
 * Возвращает информацию о пользователе по его уникальному идентификатору (ID).
 * <p>
 * Этот эндпоинт доступен только для пользователей с ролью {@code ADMIN}.
 * </p>
 *
 * @param id Уникальный идентификатор пользователя.
 * @return Объект {@link UserDto}, содержащий данные найденного пользователя.
 * @throws com.example.bankcards.exception.NotFoundException если пользователь с указанным ID не найден.
 * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не имеет роли ADMIN.
 * @see com.example.bankcards.service.UserService#findUserById(Long)
 */
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

    /**
     * Создает нового пользователя в системе.
     * <p>
     * Этот эндпоинт доступен только для пользователей с ролью {@code ADMIN}.
     * Данные нового пользователя должны соответствовать требованиям валидации.
     * </p>
     *
     * @param user Объект {@link CreateUserDto}, содержащий данные для создания нового пользователя
     *             (например, имя пользователя, пароль, email).
     * @throws jakarta.validation.ValidationException если {@code user} не проходит валидацию
     *                                              (например слишком короткий пароль).
     * @throws org.springframework.dao.DataIntegrityViolationException если пользователь с таким именем уже существует.
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не имеет роли ADMIN.
     * @see com.example.bankcards.service.UserService#createUser(CreateUserDto)
     */
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

    /**
     * Удаляет пользователя из системы по его уникальному идентификатору.
     * <p>
     * Для выполнения этой операции требуется аутентификация и соответствующие права доступа
     * (например, роль администратора). В случае успешного удаления возвращается HTTP статус 204 No Content,
     * указывающий на успешное выполнение операции без возврата содержимого.
     * </p>
     *
     * @param id Уникальный идентификатор (ID) пользователя, которого необходимо удалить.
     *           Должен быть положительным числом, соответствующим существующему пользователю.
     * @throws org.springframework.security.access.AccessDeniedException если текущий аутентифицированный пользователь
     *                                                                   не имеет достаточных прав для выполнения операции (HTTP 403 Forbidden).
     * @throws org.springframework.security.core.AuthenticationException если пользователь не авторизован (не предоставлен токен или он недействителен)
     *                                                                   (HTTP 401 Unauthorized).
     * @see com.example.bankcards.service.UserService#deleteUser(Long)
     */
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
