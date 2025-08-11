package com.example.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Date;

import java.time.YearMonth;

/**
 * <p>Конвертер JPA для преобразования между {@link java.time.YearMonth} и {@link java.sql.Date}.</p>
 *
 * <p>Этот класс реализует {@link jakarta.persistence.AttributeConverter} для автоматического
 * преобразования объектов {@link java.time.YearMonth} в {@link java.sql.Date} при сохранении
 * в базу данных и обратно при чтении.</p>
 *
 * <p>Поскольку класс аннотирован {@code @Converter(autoApply = true)}, этот конвертер будет
 * автоматически применяться ко всем полям типа {@link java.time.YearMonth} в сущностях JPA,
 * если для конкретного поля явно не указан другой конвертер.</p>
 *
 * <p>При преобразовании {@link java.time.YearMonth} в {@link java.sql.Date}, в базу данных
 * сохраняется дата, соответствующая <b>первому дню месяца</b>. Например, {@code YearMonth.of(2023, 10)}
 * будет сохранено как {@code 2023-10-01}. При обратном преобразовании из {@link java.sql.Date}
 * извлекаются только год и месяц, игнорируя день.</p>
 *
 * @see jakarta.persistence.AttributeConverter
 * @see java.time.YearMonth
 * @see java.sql.Date
 */
@Converter(autoApply = true)
public class YearMonthDateConverter implements AttributeConverter<YearMonth, Date> {
    /**
     * <p>Преобразует объект {@link java.time.YearMonth} в {@link java.sql.Date} для сохранения в базе данных.</p>
     *
     * <p>Если входное значение {@code attribute} не равно {@code null}, метод создает
     * {@link java.sql.Date}, представляющую <b>первый день месяца</b> из {@code YearMonth}.</p>
     * <p>Например, {@code YearMonth.of(2023, 5)} будет преобразован в {@code 2023-05-01}.</p>
     *
     * @param attribute Объект {@link java.time.YearMonth}, который нужно преобразовать. Может быть {@code null}.
     * @return Объект {@link java.sql.Date}, представляющий первый день месяца,
     *         или {@code null}, если входное значение было {@code null}.
     */
    @Override
    public Date convertToDatabaseColumn(YearMonth attribute) {
        if (attribute != null) {
            return java.sql.Date.valueOf(attribute.atDay(1));
        }
        return null;
    }

    /**
     * <p>Преобразует объект {@link java.sql.Date} из базы данных обратно в {@link java.time.YearMonth}.</p>
     *
     * <p>Если входное значение {@code dbData} не равно {@code null}, метод извлекает
     * год и месяц из {@link java.sql.Date} и создает соответствующий {@link java.time.YearMonth}.
     * День месяца из {@code dbData} игнорируется.</p>
     *
     * @param dbData Объект {@link java.sql.Date}, полученный из базы данных. Может быть {@code null}.
     * @return Объект {@link java.time.YearMonth}, извлеченный из даты,
     *         или {@code null}, если входное значение было {@code null}.
     */
    @Override
    public YearMonth convertToEntityAttribute(Date dbData) {
        if (dbData != null) {
            return YearMonth.from(dbData.toLocalDate());
        }
        return null;
    }
}
