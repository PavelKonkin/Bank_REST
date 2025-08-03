package com.example.bankcards.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumNameValidator implements ConstraintValidator<ValidRole, String> {
    private Set<String> allowedValues;

    @Override
    public void initialize(ValidRole constraintAnnotation) {
        Class<? extends Enum<?>> enumClass = constraintAnnotation.enumClass();

        allowedValues = Stream.of(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());

        String allowedValuesString = String.join(", ", allowedValues);
        constraintAnnotation.message().replace("{allowedValues}", allowedValuesString);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return allowedValues.contains(value);
    }
}
