package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransactionDto {
    @NotNull(message = "ID карты-отправителя не может быть пустым")
    private Long fromCardId;

    @NotNull(message = "ID карты-получателя не может быть пустым")
    private Long toCardId;

    @NotNull(message = "Сумма перевода не может быть пустой")
    @Positive(message = "Сумма перевода должна быть положительной")
    private BigDecimal amount;
}
