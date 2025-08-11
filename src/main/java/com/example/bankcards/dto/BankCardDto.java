package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Представляет собой объект передачи данных (DTO) для публичной информации о банковской карте.
 * <p>
 * Этот класс используется для передачи нечувствительных деталей банковской карты клиенту,
 * таких как маскированный номер карты, срок действия, текущий баланс и статус.
 * Он предназначен для отображения информации, которую можно безопасно раскрывать.
 * </p>
 * <p>
 * Аннотация {@code @Data} от Lombok автоматически генерирует геттеры, сеттеры,
 * методы {@code equals()}, {@code hashCode()} и {@code toString()} для всех полей.
 * </p>
 */
@Data
@Schema(description = "DTO с публичной информацией о банковской карте")
public class BankCardDto {
    /**
     * Уникальный идентификатор банковской карты.
     * Это поле генерируется системой и доступно только для чтения.
     */
    @Schema(description = "Уникальный ID карты",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "42")
    private Long id;

    /**
     * Маскированный номер банковской карты.
     * Обычно последние 4 цифры открыты, а остальные скрыты (например, "**** **** **** 5678").
     * Это поле доступно только для чтения, чтобы обеспечить безопасность данных.
     */
    @Schema(description = "Маскированный номер карты",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "**** **** **** 5678")
    private String maskedCardNumber;

    /**
     * Срок действия карты в формате ММ/ГГГГ (например, "12/2028").
     * Это поле доступно только для чтения.
     */
    @Schema(description = "Срок действия в формате ММ/ГГГГ",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "12/2028")
    private String expiryDate;

    /**
     * Текущий баланс на банковской карте.
     * Представлен как {@link java.math.BigDecimal} для точных финансовых расчетов.
     * Это поле доступно только для чтения.
     */
    @Schema(description = "Текущий баланс на карте",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "15300.50")
    private BigDecimal balance;

    /**
     * Текущий статус банковской карты.
     * Возможные значения могут включать "ACTIVE" (активна), "BLOCKED" (заблокирована),
     * "EXPIRED" (срок действия истек) и другие, определенные в системе.
     * Это поле доступно только для чтения.
     */
    @Schema(description = "Статус карты (ACTIVE, BLOCKED, EXPIRED)",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "ACTIVE")
    private String status;

    /**
     * Идентификатор владельца банковской карты.
     * Это поле связывает карту с конкретным пользователем в системе.
     * Это поле доступно только для чтения.
     */
    @Schema(description = "ID владельца карты",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "1")
    private Long ownerId;
}
