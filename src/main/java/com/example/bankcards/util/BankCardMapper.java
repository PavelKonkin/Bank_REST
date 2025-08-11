package com.example.bankcards.util;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Интерфейс-маппер MapStruct для преобразования объектов, связанных с банковскими картами.
 * Обеспечивает преобразование между сущностями {@link BankCard} и различными DTO:
 * {@link BankCardDto} для представления данных карты и {@link CreateBankCardDto} для создания новых карт.
 * <p>
 * Использует {@code componentModel = "spring"}, что позволяет Spring автоматически обнаруживать
 * и внедрять реализацию этого маппера.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface BankCardMapper {
    /**
     * Преобразует сущность {@link BankCard} в объект передачи данных {@link BankCardDto}.
     * Выполняет следующие преобразования:
     * <ul>
     *     <li>Поле {@code expiryDate} ({@link YearMonth}) преобразуется в строку формата "MM/yyyy" с помощью {@code yearMonthToString}.</li>
     *     <li>Поле {@code status} ({@link CardStatus}) преобразуется в строковое представление названия статуса с помощью {@code statusToString}.</li>
     *     <li>Идентификатор владельца карты ({@code owner.id}) отображается в поле {@code ownerId}.</li>
     * </ul>
     *
     * @param bankCard Сущность банковской карты для преобразования.
     * @return Объект {@link BankCardDto}, содержащий данные карты для внешнего представления.
     */
    @Mapping(source = "maskedCardNumber", target = "maskedCardNumber")
    @Mapping(source = "expiryDate", target = "expiryDate", qualifiedByName = "yearMonthToString")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(source = "owner.id", target = "ownerId")
    BankCardDto toDto(BankCard bankCard);

    /**
     * Преобразует объект {@link CreateBankCardDto} в сущность {@link BankCard}.
     * Этот метод используется для создания новой сущности карты из данных, предоставленных пользователем.
     * <ul>
     *     <li>Поля {@code id}, {@code balance}, {@code status} и {@code owner} игнорируются при маппинге,
     *         так как они обычно устанавливаются на уровне сервиса или базы данных при создании.</li>
     *     <li>Поле {@code expiryDate} (строка) преобразуется в {@link YearMonth} с помощью {@code stringToYearMonth}.</li>
     * </ul>
     *
     * @param createDto Объект DTO, содержащий данные для создания новой банковской карты.
     * @return Сущность {@link BankCard}, готовая для сохранения в базе данных.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(source = "expiryDate", target = "expiryDate", qualifiedByName = "stringToYearMonth")
    BankCard toEntity(CreateBankCardDto createDto);

    /**
     * Вспомогательный метод MapStruct для преобразования объекта {@link YearMonth}
     * в строковое представление формата "MM/yyyy".
     * Используется для форматирования даты истечения срока действия карты при преобразовании в DTO.
     *
     * @param expiryDate Дата истечения срока действия в формате {@link YearMonth}.
     * @return Отформатированная строка даты истечения срока действия (например, "12/2025")
     * или {@code null}, если входное значение {@code null}.
     */
    @Named("yearMonthToString")
    default String yearMonthToString(YearMonth expiryDate) {
        if (expiryDate == null) return null;
        return expiryDate.format(DateTimeFormatter.ofPattern("MM/yyyy"));
    }

    /**
     * Вспомогательный метод MapStruct для преобразования строкового представления даты
     * в объект {@link YearMonth}. Ожидает формат "MM/yy".
     * Используется при создании сущности карты из DTO.
     *
     * @param expiryDate Строка даты истечения срока действия (например, "12/25").
     * @return Объект {@link YearMonth} или {@code null}, если входное значение {@code null}.
     */
    @Named("stringToYearMonth")
    default YearMonth stringToYearMonth(String expiryDate) {
        if (expiryDate == null) return null;
        return YearMonth.parse(expiryDate, DateTimeFormatter.ofPattern("MM/yy"));
    }

    /**
     * Вспомогательный метод MapStruct для преобразования перечисления {@link CardStatus}
     * в его строковое имя.
     * Используется для представления статуса карты в DTO.
     *
     * @param status Статус карты в виде перечисления {@link CardStatus}.
     * @return Строковое имя статуса (например, "ACTIVE", "BLOCKED")
     *         или {@code null}, если входное значение {@code null}.
     */
    @Named("statusToString")
    default String statusToString(CardStatus status) {
        if (status == null) return null;
        return status.name();
    }
}
