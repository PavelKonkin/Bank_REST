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

@RestController
@RequestMapping("/api/v1/bankcards")
@Tag(name = "Управление банковскими картами", description = "API для создания, получения и удаления банковских карт.")
@SecurityRequirement(name = "bearerAuth")
public class BankCardController {
    private final BankCardsService bankCardsService;

    public BankCardController(BankCardsService bankCardsService) {
        this.bankCardsService = bankCardsService;
    }

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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить все карты с пагинацией (ADMIN)",
            description = "Возвращает страницу со всеми существующими картами. Доступно только администраторам.")
    public Page<BankCardDto> getAllCards(
            @Parameter(description = "Параметры пагинации и сортировки") Pageable pageable) {
        return bankCardsService.getAllCards(pageable);
    }

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

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить карту по ID (ADMIN)",
            description = "Безвозвратно удаляет карту из системы. Доступно только администраторам.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public void deleteCard(@Parameter(description = "ID удаляемой карты") @PathVariable Long cardId) {
        bankCardsService.delete(cardId);
    }

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
