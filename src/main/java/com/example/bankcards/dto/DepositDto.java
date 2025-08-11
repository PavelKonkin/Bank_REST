package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Объект передачи данных (DTO) для операций пополнения (депозита) счета.
 * Этот класс используется для инкапсуляции информации, необходимой для выполнения
 * операции пополнения, а именно, суммы, которую необходимо внести.
 * Включает аннотации для валидации данных.
 */
@Data
public class DepositDto {
    /**
     * Сумма пополнения счета.
     * Это поле является обязательным и не может быть пустым.
     * Сумма должна быть положительным числом, что гарантируется аннотацией {@link Positive}.
     * Используется {@link BigDecimal} для точного представления денежных сумм,
     * избегая проблем с точностью, присущих типам float и double.
     */
    @NotNull(message = "Сумма пополнения не может быть пустой")
    @Positive(message = "Сумма пополнения должна быть положительной")
    private BigDecimal amount;
}
