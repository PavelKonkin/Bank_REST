package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO с публичной информацией о банковской карте")
public class BankCardDto {
    @Schema(description = "Уникальный ID карты",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "42")
    private Long id;

    @Schema(description = "Маскированный номер карты",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "**** **** **** 5678")
    private String maskedCardNumber;

    @Schema(description = "Срок действия в формате ММ/ГГГГ",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "12/2028")
    private String expiryDate;

    @Schema(description = "Текущий баланс на карте",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "15300.50")
    private BigDecimal balance;

    @Schema(description = "Статус карты (ACTIVE, BLOCKED, EXPIRED)",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "ACTIVE")
    private String status;

    @Schema(description = "ID владельца карты",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "1")
    private Long ownerId;
}
