package com.example.bankcards.service;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardDto;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.YearMonth;

public interface BankCardsService {
    void create(CreateBankCardDto createDto);

    Page<BankCardDto> getAllCards(Pageable pageable);

    void blockCard(Long cardId);

    void activateCard(Long cardId);

    void delete(Long cardId);

    void requestBlock(Long cardId);

    Page<BankCardDto> searchCurrentUserCards(CardStatus status, BigDecimal minBalance, YearMonth expiryDateFrom, Pageable pageable);
}
