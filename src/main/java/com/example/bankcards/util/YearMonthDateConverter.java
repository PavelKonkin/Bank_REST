package com.example.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Date;

import java.time.YearMonth;

@Converter(autoApply = true)
public class YearMonthDateConverter implements AttributeConverter<YearMonth, Date> {
    @Override
    public Date convertToDatabaseColumn(YearMonth attribute) {
        if (attribute != null) {
            return java.sql.Date.valueOf(attribute.atDay(1));
        }
        return null;
    }

    @Override
    public YearMonth convertToEntityAttribute(Date dbData) {
        if (dbData != null) {
            return YearMonth.from(dbData.toLocalDate());
        }
        return null;
    }
}
