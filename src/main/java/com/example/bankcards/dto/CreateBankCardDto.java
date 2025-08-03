package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.CreditCardNumber;

@Data
@Schema(description = "DTO для создания новой банковской карты")
public class CreateBankCardDto {
    @Schema(description = "Номер банковской карты (16 цифр без пробелов)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "4276160012345678")
    @NotBlank(message = "Номер карты не может быть пустым")
    @CreditCardNumber(message = "Неверный формат номера банковской карты")
    private String cardNumber;

    @Schema(description = "Срок действия в формате ММ/ГГ (например, 12/28)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "12/28")
    @NotBlank(message = "Срок действия не может быть пустым")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/(\\d{2})$", message = "Неверный формат срока действия. Используйте ММ/ГГ.")
    private String expiryDate;

    @Schema(description = "ID владельца карты",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1")
    @NotNull(message = "ID владельца не может быть пустым")
    private Long ownerId;
}
