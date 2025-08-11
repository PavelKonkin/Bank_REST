package com.example.bankcards.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Реализация {@link ConstraintValidator}, предназначенная для проверки,
 * соответствует ли строковое значение одному из допустимых имен (констант)
 * указанного перечисления ({@code enum}).</p>
 *
 * <p>Этот валидатор используется совместно с пользовательской аннотацией
 * {@link ValidRole} (или любой другой, которая использует его),
 * чтобы гарантировать, что строковое поле содержит значение,
 * которое является действительным именем одной из констант перечисления.</p>
 *
 * <p>Он извлекает все имена констант из заданного класса перечисления
 * и проверяет входящую строку на их наличие.</p>
 *
 * @see ValidRole
 * @see ConstraintValidator
 * @see Enum
 */
public class EnumNameValidator implements ConstraintValidator<ValidRole, String> {
    /**
     * Набор ({@link Set}) допустимых строковых значений,
     * которые соответствуют именам констант перечисления.
     * Эти значения инициализируются в методе {@link #initialize(ValidRole)}.
     */
    private Set<String> allowedValues;

    /**
     * Инициализирует валидатор, извлекая допустимые строковые значения
     * из констант перечисления, указанного в аннотации {@code ValidRole}.
     *
     * <p>Этот метод вызывается фреймворком валидации после создания экземпляра валидатора.</p>
     *
     * @param constraintAnnotation Аннотация {@link ValidRole},
     *                             которая содержит класс перечисления для валидации.
     */
    @Override
    public void initialize(ValidRole constraintAnnotation) {
        Class<? extends Enum<?>> enumClass = constraintAnnotation.enumClass();

        allowedValues = Stream.of(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());

        String allowedValuesString = String.join(", ", allowedValues);
        constraintAnnotation.message().replace("{allowedValues}", allowedValuesString);
    }

    /**
     * Проверяет, является ли переданное строковое значение допустимым именем константы перечисления.
     *
     * <p>Если {@code value} равно {@code null}, метод возвращает {@code true},
     * так как проверка на {@code null} обычно обрабатывается отдельной аннотацией
     * (например, {@code @NotNull}).</p>
     *
     * @param value   Строковое значение, подлежащее валидации.
     * @param context Контекст валидации, предоставляемый фреймворком (не используется в данной реализации).
     * @return {@code true}, если {@code value} является {@code null} или содержится
     *         в наборе {@link #allowedValues}; {@code false} в противном случае.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return allowedValues.contains(value);
    }
}
