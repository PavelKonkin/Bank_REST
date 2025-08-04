package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CreateTransactionDto;
import com.example.bankcards.dto.DepositDto;
import com.example.bankcards.dto.TotalBalanceDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.TransactionService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final BankCardsRepository bankCardsRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(BankCardsRepository bankCardsRepository, UserRepository userRepository) {
        this.bankCardsRepository = bankCardsRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void createTransaction(CreateTransactionDto transactionDto) {
        Long fromCardId = transactionDto.getFromCardId();
        Long toCardId = transactionDto.getToCardId();
        BigDecimal amount = transactionDto.getAmount();
        long currentUserId = getAuthenticatedUser().getId();

        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Карта-отправитель и карта-получатель не могут быть одинаковыми.");
        }

        BankCard fromCard = bankCardsRepository.findById(fromCardId)
                .orElseThrow(() -> new NotFoundException("Карта-отправитель с ID " + fromCardId + " не найдена."));

        BankCard toCard = bankCardsRepository.findById(toCardId)
                .orElseThrow(() -> new NotFoundException("Карта-получатель с ID " + toCardId + " не найдена."));

        if (!fromCard.getOwner().getId().equals(currentUserId) || !toCard.getOwner().getId().equals(currentUserId)) {
            throw new BadCredentialsException("Вы не являетесь владельцем карты.");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств на карте-отправителе.");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));
    }

    @Override
    @Transactional(readOnly = true)
    public TotalBalanceDto calculateTotalBalanceForUser() {
        User currentUser = getAuthenticatedUser();
        BigDecimal totalBalance = bankCardsRepository.calculateTotalBalanceByUserId(currentUser.getId());

        BigDecimal result = Optional.ofNullable(totalBalance).orElse(BigDecimal.ZERO);

        return new TotalBalanceDto(result);
    }

    @Override
    @Transactional
    public void depositToCard(Long cardId, DepositDto depositDto) {
        BankCard card = bankCardsRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта с ID " + cardId + " не найдена."));
        User currentUser = getAuthenticatedUser();

        if (!card.getOwner().getId().equals(currentUser.getId())) {
            throw new BadCredentialsException("Вы не являетесь владельцем данной карты.");
        }

        BigDecimal amount = depositDto.getAmount();
        card.setBalance(card.getBalance().add(amount));
    }

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Аутентифицированный пользователь " + username
                        + " не найден в системе"));
    }
}
