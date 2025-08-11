package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.CreditCardNumber;

/**
 * Представляет собой объект передачи данных (DTO) для запроса на создание новой банковской карты.
 * <p>
 * Этот класс используется для инкапсуляции данных, необходимых для инициализации новой банковской карты
 * в системе, таких как номер карты, срок действия и идентификатор владельца.
 * </p>
 * <p>
 * Аннотации валидации (например, {@code @NotBlank}, {@code @Pattern}, {@code @CreditCardNumber})
 * обеспечивают проверку входных данных до их обработки бизнес-логикой.
 * Аннотация {@code @Data} от Lombok автоматически генерирует геттеры, сеттеры,
 * методы {@code equals()}, {@code hashCode()} и {@code toString()} для всех полей.
 * </p>
 */
@Data
@Schema(description = "DTO для создания новой банковской карты")
public class CreateBankCardDto {
    /**
     * Номер банковской карты.
     * <p>
     * Должен состоять из 16 цифр без пробелов. Это поле является обязательным для заполнения.
     * </p>
     * <p>
     * Валидация:
     * <ul>
     *     <li>{@code @NotBlank}: Не может быть пустым или состоять только из пробелов.</li>
     *     <li>{@code @CreditCardNumber}: Проверяет, что номер соответствует формату кредитной карты (алгоритм Луна).</li>
     * </ul>
     */
    @Schema(description = "Номер банковской карты (16 цифр без пробелов)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "4276160012345678")
    @NotBlank(message = "Номер карты не может быть пустым")
    @CreditCardNumber(message = "Неверный формат номера банковской карты")
    private String cardNumber;

    /**
     * Срок действия карты в формате ММ/ГГ (например, "12/28").
     * <p>
     * Это поле является обязательным для заполнения.
     * </p>
     * <p>
     * Валидация:
     * <ul>
     *     <li>{@code @NotBlank}: Не может быть пустым или состоять только из пробелов.</li>
     *     <li>{@code @Pattern}: Должен соответствовать формату "ММ/ГГ", где ММ - месяц (01-12), ГГ - последние две цифры года.</li>
     * </ul>
     */
    @Schema(description = "Срок действия в формате ММ/ГГ (например, 12/28)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "12/28")
    @NotBlank(message = "Срок действия не может быть пустым")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/(\\d{2})$", message = "Неверный формат срока действия. Используйте ММ/ГГ.")
    private String expiryDate;

    /**
     * Уникальный идентификатор владельца банковской карты.
     * <p>
     * Это поле связывает новую карту с существующим пользователем в системе. Является обязательным.
     * </p>
     * <p>
     * Валидация:
     * <ul>
     *     <li>{@code @NotNull}: Не может быть null.</li>
     * </ul>
     */
    @Schema(description = "ID владельца карты",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1")
    @NotNull(message = "ID владельца не может быть пустым")
    private Long ownerId;
}
