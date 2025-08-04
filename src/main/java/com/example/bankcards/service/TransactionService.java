package com.example.bankcards.service;

import com.example.bankcards.dto.CreateTransactionDto;
import com.example.bankcards.dto.DepositDto;
import com.example.bankcards.dto.TotalBalanceDto;

public interface TransactionService {
    void createTransaction(CreateTransactionDto transactionDto);

    TotalBalanceDto calculateTotalBalanceForUser();

    void depositToCard(Long cardId, DepositDto depositDto);
}
