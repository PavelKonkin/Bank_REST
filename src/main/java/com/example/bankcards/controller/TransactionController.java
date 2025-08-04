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

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transactions", description = "Операции по переводам и балансу")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

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

    @GetMapping("/balance")
    @Operation(summary = "Получить общий баланс по всем картам",
            description = "Возвращает суммарный баланс по всем картам текущего аутентифицированного пользователя.")
    @ApiResponse(responseCode = "200", description = "Баланс успешно получен",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TotalBalanceDto.class)))
    public TotalBalanceDto getTotalBalance() {
        return transactionService.calculateTotalBalanceForUser();
    }

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
