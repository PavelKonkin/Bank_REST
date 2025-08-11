package com.example.bankcards.controller;

import com.example.bankcards.dto.DepositDto;
import com.example.bankcards.dto.TotalBalanceDto;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.dto.CreateTransactionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для управления финансовыми операциями и балансом.
 * <p>
 * Предоставляет конечные точки (endpoints) для выполнения переводов между картами,
 * пополнения баланса карты и получения общего баланса по всем картам текущего
 * аутентифицированного пользователя. Все операции требуют аутентификации пользователя.
 * </p>
 * Базовый путь для всех эндпоинтов в этом контроллере: {@code /api/v1}.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transactions", description = "Операции по переводам и балансу")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    private final TransactionService transactionService;

    /**
     * Конструктор для {@code TransactionController}.
     * <p>
     * Внедряет зависимость {@link TransactionService}, которая отвечает
     * за бизнес-логику, связанную с транзакциями и балансом карт.
     * </p>
     * @param transactionService Сервис, предоставляющий функциональность для работы с транзакциями.
     */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Создает новую транзакцию перевода средств с одной карты на другую.
     * <p>
     * Пользователь, выполняющий запрос, должен быть владельцем карты-отправителя.
     * Сумма перевода должна быть положительной и не превышать текущий баланс
     * карты-отправителя.
     * </p>
     *
     * @param transactionDto Объект {@link CreateTransactionDto}, содержащий детали транзакции:
     *                       ID карты-отправителя, ID карты-получателя и сумму перевода.
     * @throws jakarta.validation.ValidationException если {@code transactionDto} не проходит валидацию (например, отрицательная сумма, некорректные ID).
     * @throws com.example.bankcards.exception.NotFoundException если одна из карт (отправителя или получателя) не найдена.
     * @throws java.lang.IllegalArgumentException если на карте-отправителе недостаточно средств для совершения перевода.
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не является владельцем карты-отправителя.
     * @see com.example.bankcards.service.TransactionService#createTransaction(CreateTransactionDto)
     */
    @PostMapping("/transactions")
    @Operation(summary = "Создать перевод с карты на карту",
            description = "Выполняет перевод средств между двумя картами. " +
                    "Пользователь должен быть владельцем карты-отправителя. " +
                    "Сумма перевода не может превышать баланс карты-отправителя.")
    @ApiResponse(responseCode = "200", description = "Перевод успешно создан")
    @ApiResponse(responseCode = "400", description = "Неверные данные запроса (например, отрицательная сумма, недостаточный баланс)")
    @ApiResponse(responseCode = "403", description = "Доступ запрещен (пользователь не является владельцем карты-отправителя)")
    @ApiResponse(responseCode = "404", description = "Одна из карт не найдена")
    public void createTransaction(
            @Valid @RequestBody CreateTransactionDto transactionDto) {

        transactionService.createTransaction(transactionDto);

    }

    /**
     * Возвращает общий суммарный баланс по всем банковским картам, принадлежащим текущему аутентифицированному пользователю.
     * <p>
     * Этот эндпоинт агрегирует балансы всех активных карт пользователя для получения общей суммы.
     * </p>
     *
     * @return Объект {@link TotalBalanceDto}, содержащий общую сумму балансов всех карт пользователя.
     * @see com.example.bankcards.service.TransactionService#calculateTotalBalanceForUser()
     */
    @GetMapping("/balance")
    @Operation(summary = "Получить общий баланс по всем картам",
            description = "Возвращает суммарный баланс по всем картам текущего аутентифицированного пользователя.")
    @ApiResponse(responseCode = "200", description = "Баланс успешно получен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TotalBalanceDto.class)))
    public TotalBalanceDto getTotalBalance() {
        return transactionService.calculateTotalBalanceForUser();
    }

    /**
     * Пополняет баланс указанной банковской карты.
     * <p>
     * Текущий аутентифицированный пользователь должен быть владельцем карты,
     * на которую производится пополнение. Сумма пополнения должна быть положительной.
     * </p>
     *
     * @param cardId     Уникальный идентификатор (ID) карты, которую необходимо пополнить.
     * @param depositDto Объект {@link DepositDto}, содержащий сумму для пополнения.
     * @throws jakarta.validation.ValidationException если {@code depositDto} не проходит валидацию (например, отрицательная сумма).
     * @throws com.example.bankcards.exception.NotFoundException если карта с указанным ID не найдена.
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не является владельцем указанной карты.
     * @see com.example.bankcards.service.TransactionService#depositToCard(Long, DepositDto)
     */
    @PostMapping("/cards/{id}/deposit")
    @Operation(summary = "Пополнить баланс карты",
            description = "Увеличивает баланс указанной карты. Пользователь должен быть владельцем карты.")
    @ApiResponse(responseCode = "200", description = "Баланс успешно пополнен")
    @ApiResponse(responseCode = "400", description = "Неверные данные запроса (например, отрицательная сумма)")
    @ApiResponse(responseCode = "403", description = "Доступ запрещен (пользователь не является владельцем карты)")
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
    public void depositToCard(
            @PathVariable("id") Long cardId,
            @Valid @RequestBody DepositDto depositDto) {

        transactionService.depositToCard(cardId, depositDto);
    }
}
