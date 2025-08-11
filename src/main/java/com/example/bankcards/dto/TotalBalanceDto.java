package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Объект передачи данных (DTO) для представления общего баланса.
 * Этот класс используется для инкапсуляции и передачи суммарного баланса
 * по всем счетам или картам пользователя.
 * <p>
 * Используется в качестве ответа на запросы, которые требуют отображения
 * агрегированного финансового состояния.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalBalanceDto {
    /**
     * Общий баланс.
     * Представляет собой суммарное значение денежных средств,
     * доступных на всех связанных счетах или картах.
     * Используется {@link BigDecimal} для точного представления денежных сумм,
     * избегая проблем с точностью, присущих типам float и double.
     */
    private BigDecimal totalBalance;
}
