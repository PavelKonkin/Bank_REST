package com.example.bankcards.service.impl;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.BankCardSpecification;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.BankCardsService;
import com.example.bankcards.util.BankCardMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.util.Base64;

@Service
public class BankCardsServiceImpl implements BankCardsService {
    private final BankCardSpecification bankCardSpecification;
    private final BankCardsRepository bankCardsRepository;
    private final UserRepository userRepository;
    private final BankCardMapper bankCardMapper;
    @Value("${encryption.hash-pepper}")
    private String hashPepper;

    public BankCardsServiceImpl(BankCardSpecification bankCardSpecification, BankCardsRepository bankCardsRepository,
                                UserRepository userRepository, BankCardMapper bankCardMapper) {
        this.bankCardSpecification = bankCardSpecification;
        this.bankCardsRepository = bankCardsRepository;
        this.userRepository = userRepository;
        this.bankCardMapper = bankCardMapper;
    }

    @Override
    @Transactional
    public void create(CreateBankCardDto createDto) {
        User owner = userRepository.findById(createDto.getOwnerId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + createDto.getOwnerId() + " не найден"));

        BankCard newCard = bankCardMapper.toEntity(createDto);
        newCard.setOwner(owner);
        newCard.setBalance(BigDecimal.ZERO);
        newCard.setStatus(CardStatus.ACTIVE);

        String plainCardNumber = createDto.getCardNumber();

        newCard.setCardNumberHash(createHashedIndex(plainCardNumber));

        try {
            bankCardsRepository.save(newCard);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Карта с таким номером уже существует.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BankCardDto> getAllCards(Pageable pageable) {
        return bankCardsRepository.findAll(pageable)
                .map(bankCardMapper::toDto);
    }

    @Override
    @Transactional
    public void blockCard(Long cardId) {
        changeCardStatus(cardId, CardStatus.BLOCKED);
    }

    @Override
    @Transactional
    public void activateCard(Long cardId) {
        changeCardStatus(cardId, CardStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void delete(Long cardId) {
        bankCardsRepository.deleteById(cardId);
    }

    @Override
    @Transactional
    public void requestBlock(Long cardId) {
        User currentUser = getAuthenticatedUser();
        BankCard card = findCardByIdOrThrow(cardId);

        if (!card.getOwner().getId().equals(currentUser.getId())) {
            throw new BadCredentialsException("Доступ запрещен: вы не являетесь владельцем данной карты.");
        }

        card.setStatus(CardStatus.BLOCKED);
        bankCardsRepository.save(card);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BankCardDto> searchCurrentUserCards(CardStatus status, BigDecimal minBalance,
                                                    YearMonth expiryDateFrom, Pageable pageable) {
        User currentUser = getAuthenticatedUser();

        Specification<BankCard> spec = bankCardSpecification
                .build(currentUser.getId(), status, minBalance, expiryDateFrom);

        Page<BankCard> cardsPage = bankCardsRepository.findAll(spec, pageable);

        return cardsPage.map(bankCardMapper::toDto);
    }

    private void changeCardStatus(Long cardId, CardStatus newStatus) {
        BankCard card = findCardByIdOrThrow(cardId);

        if (card.getStatus() != newStatus) {
            card.setStatus(newStatus);
            bankCardsRepository.save(card);
        }
    }

    private BankCard findCardByIdOrThrow(Long cardId) {
        return bankCardsRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта с ID " + cardId + " не найдена"));
    }

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Аутентифицированный пользователь " + username
                        + " не найден в системе"));
    }

    private String createHashedIndex(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String textWithPepper = plainText + hashPepper;

            byte[] hash = digest.digest(textWithPepper.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new NotFoundException("Не удалось найти алгоритм хэширования");
        }
    }
}
