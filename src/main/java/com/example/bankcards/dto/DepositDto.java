package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositDto {
    @NotNull(message = "Сумма пополнения не может быть пустой")
    @Positive(message = "Сумма пополнения должна быть положительной")
    private BigDecimal amount;
}
