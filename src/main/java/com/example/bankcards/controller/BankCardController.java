package com.example.bankcards.controller;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.BankCardsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * REST контроллер для управления банковскими картами.
 * <p>
 * Предоставляет конечные точки (endpoints) для выполнения операций,
 * связанных с банковскими картами, таких как активация, удаление,
 * просмотр и блокировка. Различные операции доступны в зависимости
 * от роли пользователя ({@code ADMIN} или {@code USER}).
 * </p>
 * Базовый путь для всех эндпоинтов в этом контроллере: {@code /api/v1/bank-cards}.
 */
@RestController
@RequestMapping("/api/v1/bankcards")
@Tag(name = "Управление банковскими картами", description = "API для создания, получения и удаления банковских карт.")
@SecurityRequirement(name = "bearerAuth")
public class BankCardController {
    private final BankCardsService bankCardsService;

    /**
     * Конструктор для {@code BankCardController}.
     * <p>
     * Внедряет зависимость {@link BankCardsService}, которая отвечает
     * за бизнес-логику работы с банковскими картами.
     * </p>
     * @param bankCardsService Сервис, предоставляющий функциональность для работы с банковскими картами.
     */
    public BankCardController(BankCardsService bankCardsService) {
        this.bankCardsService = bankCardsService;
    }

    /**
     * Создает новую банковскую карту для указанного пользователя.
     * <p>
     * Эта операция доступна только пользователям с ролью 'ADMIN'.
     * Принимает объект {@link CreateBankCardDto}, содержащий данные для новой карты,
     * такие как ID владельца, тип карты, лимит и срок действия.
     * </p>
     * <p>
     * В случае успешного создания возвращает статус {@code 201 Created}.
     * </p>
     *
     * @param createDto Объект {@link CreateBankCardDto}, содержащий данные для создания новой карты.
     *                  Должен быть валидным (проверяется с помощью {@link Valid}).
     * @throws org.springframework.web.server.ResponseStatusException
     *         <ul>
     *             <li>{@code HttpStatus.BAD_REQUEST} (400) если данные в {@code createDto} невалидны.</li>
     *             <li>{@code HttpStatus.NOT_FOUND} (404) если пользователь-владелец не найден.</li>
     *             <li>{@code HttpStatus.CONFLICT} (409) если карта с таким номером уже существует.</li>
     *         </ul>
     * @see CreateBankCardDto
     * @see BankCardsService#create(CreateBankCardDto)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новую банковскую карту (ADMIN)",
            description = "Создает новую карту для указанного пользователя. Доступно только администраторам.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Карта успешно создана"),
            @ApiResponse(responseCode = "400", description = "Неверные данные в запросе"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (не администратор)"),
            @ApiResponse(responseCode = "404", description = "Пользователь-владелец не найден"),
            @ApiResponse(responseCode = "409", description = "Карта с таким номером уже существует")
    })
    public void createCard(@Valid @RequestBody CreateBankCardDto createDto) {
        bankCardsService.create(createDto);
    }

    /**
     * Возвращает страницу со всеми существующими банковскими картами.
     * <p>
     * Эта операция доступна только пользователям с ролью 'ADMIN'.
     * Результаты могут быть пагинированы и отсортированы с помощью параметров {@link Pageable}.
     * </p>
     *
     * @param pageable Объект {@link Pageable}, содержащий информацию о пагинации (номер страницы, размер страницы)
     *                 и сортировке.
     * @return {@link Page} объектов {@link BankCardDto}, представляющих банковские карты.
     * @see BankCardDto
     * @see BankCardsService#getAllCards(Pageable)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить все карты с пагинацией (ADMIN)",
            description = "Возвращает страницу со всеми существующими картами. Доступно только администраторам.")
    public Page<BankCardDto> getAllCards(
            @Parameter(description = "Параметры пагинации и сортировки") Pageable pageable) {
        return bankCardsService.getAllCards(pageable);
    }

    /**
     * Блокирует банковскую карту по её идентификатору.
     * <p>
     * Эта операция доступна только пользователям с ролью 'ADMIN'.
     * При успешной блокировке статус карты меняется на {@link CardStatus#BLOCKED}.
     * </p>
     *
     * @param cardId Уникальный идентификатор (ID) карты, которую необходимо заблокировать.
     * @throws org.springframework.web.server.ResponseStatusException
     *         <ul>
     *             <li>{@code HttpStatus.NOT_FOUND} (404) если карта с указанным ID не найдена.</li>
     *             <li>{@code HttpStatus.FORBIDDEN} (403) если у пользователя нет достаточных прав.</li>
     *         </ul>
     * @see BankCardsService#blockCard(Long)
     * @see CardStatus
     */
    @PatchMapping("/block/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Заблокировать карту по ID (ADMIN)",
            description = "Принудительно блокирует любую карту в системе. Доступно только администраторам.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public void blockCard(@Parameter(description = "ID карты для блокировки") @PathVariable Long cardId) {
        bankCardsService.blockCard(cardId);
    }

    /**
     * Активирует ранее заблокированную или деактивированную банковскую карту по ее ID.
     * <p>
     * Эта операция доступна только пользователям с ролью {@code ADMIN}.
     * При успешной активации карта переходит в статус "активная".
     * </p>
     *
     * @param cardId Уникальный идентификатор (ID) карты, которую необходимо активировать.
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не имеет роли ADMIN.
     * @throws com.example.bankcards.exception.NotFoundException если карта с указанным ID не найдена.
     * @see com.example.bankcards.service.BankCardsService#activateCard(Long)
     */
    @PatchMapping("/activate/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Активировать карту по ID (ADMIN)",
            description = "Активирует ранее заблокированную карту. Доступно только администраторам.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно активирована"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public void activateCard(@Parameter(description = "ID карты для активации") @PathVariable Long cardId) {
        bankCardsService.activateCard(cardId);
    }

/**
 * Безвозвратно удаляет банковскую карту из системы по ее уникальному идентификатору.
 * <p>
 * Эта операция доступна только пользователям с ролью {@code ADMIN}.
 * В случае успешного удаления возвращает статус {@code 204 No Content}.
 * </p>
 *
 * @param cardId Уникальный идентификатор (ID) карты, которую необходимо удалить.
 * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не имеет роли ADMIN.
 * @see com.example.bankcards.service.BankCardsService#delete(Long)
 */
    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить карту по ID (ADMIN)",
            description = "Безвозвратно удаляет карту из системы. Доступно только администраторам.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public void deleteCard(@Parameter(description = "ID удаляемой карты") @PathVariable Long cardId) {
        bankCardsService.delete(cardId);
    }

    /**
     * Возвращает пагинированный список банковских карт, принадлежащих текущему аутентифицированному пользователю.
     * <p>
     * Поддерживает фильтрацию по статусу карты, минимальному балансу и дате истечения срока действия.
     * Доступно только пользователям с ролью {@code USER}.
     * </p>
     *
     * @param status           Необязательный параметр для фильтрации по статусу карты (например, {@link CardStatus#ACTIVE ACTIVE}, {@link CardStatus#BLOCKED BLOCKED}, {@link CardStatus#EXPIRED EXPIRED}).
     * @param minBalance       Необязательный параметр для фильтрации карт с балансом не ниже указанного.
     * @param expiryDateFrom   Необязательный параметр для фильтрации карт, срок действия которых не ранее указанной даты (формат ГГГГ-ММ).
     * @param pageable         Параметры пагинации и сортировки, предоставленные Spring Data (например, page, size, sort).
     * @return {@link org.springframework.data.domain.Page} объект, содержащий список {@link BankCardDto} и информацию о пагинации.
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не имеет роли USER.
     * @see com.example.bankcards.dto.BankCardDto
     * @see com.example.bankcards.entity.CardStatus
     * @see com.example.bankcards.service.BankCardsService#searchCurrentUserCards(CardStatus, BigDecimal, java.time.YearMonth, org.springframework.data.domain.Pageable)
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Просмотреть свои карты с фильтрацией (USER)",
            description = "Возвращает страницу со своими картами с возможностью гибкой фильтрации.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список карт успешно получен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public Page<BankCardDto> getCurrentUserCards(
            @Parameter(description = "Фильтр по статусу карты (ACTIVE, BLOCKED, EXPIRED)")
            @RequestParam(required = false) CardStatus status,

            @Parameter(description = "Фильтр по минимальному балансу (например, 1000.00)")
            @RequestParam(required = false) BigDecimal minBalance,

            @Parameter(description = "Фильтр по сроку действия (не ранее указанной даты, формат: ГГГГ-ММ)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth expiryDateFrom,

            @Parameter(description = "Параметры пагинации и сортировки") Pageable pageable) {
        return bankCardsService.searchCurrentUserCards(status, minBalance, expiryDateFrom, pageable);
    }

/**
 * Позволяет текущему аутентифицированному пользователю запросить блокировку одной из своих банковских карт.
 * <p>
 * Эта операция доступна только пользователям с ролью {@code USER}.
 * Система проверит, принадлежит ли карта запрашивающему пользователю, прежде чем выполнить блокировку.
 * Если пользователь пытается заблокировать чужую карту, будет возвращена ошибка {@code 403 Forbidden}.
 * </p>
 *
 * @param cardId Уникальный идентификатор (ID) карты, которую пользователь хочет заблокировать.
 * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не имеет роли USER,
 *                                                                   или если он пытается заблокировать карту, которая ему не принадлежит.
 * @throws com.example.bankcards.exception.NotFoundException если карта с указанным ID не найдена.
 * @see com.example.bankcards.service.BankCardsService#requestBlock(Long)
 */
    @PatchMapping("/my/block/{cardId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Запросить блокировку своей карты (USER)",
            description = "Пользователь может запросить блокировку своей карты.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (попытка заблокировать чужую карту)"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public void requestBlock(@Parameter(description = "ID карты для блокировки") @PathVariable Long cardId) {
        bankCardsService.requestBlock(cardId);
    }
}
