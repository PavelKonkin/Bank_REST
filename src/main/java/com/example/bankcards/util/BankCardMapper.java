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

@Mapper(componentModel = "spring")
public interface BankCardMapper {
    @Mapping(source = "maskedCardNumber", target = "maskedCardNumber")
    @Mapping(source = "expiryDate", target = "expiryDate", qualifiedByName = "yearMonthToString")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(source = "owner.id", target = "ownerId")
    BankCardDto toDto(BankCard bankCard);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(source = "expiryDate", target = "expiryDate", qualifiedByName = "stringToYearMonth")
    BankCard toEntity(CreateBankCardDto createDto);

    @Named("yearMonthToString")
    default String yearMonthToString(YearMonth expiryDate) {
        if (expiryDate == null) return null;
        return expiryDate.format(DateTimeFormatter.ofPattern("MM/yyyy"));
    }

    @Named("stringToYearMonth")
    default YearMonth stringToYearMonth(String expiryDate) {
        if (expiryDate == null) return null;
        return YearMonth.parse(expiryDate, DateTimeFormatter.ofPattern("MM/yy"));
    }

    @Named("statusToString")
    default String statusToString(CardStatus status) {
        if (status == null) return null;
        return status.name();
    }
}
