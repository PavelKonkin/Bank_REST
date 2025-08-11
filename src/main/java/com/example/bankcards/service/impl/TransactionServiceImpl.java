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

/**
 * Сервис {@code TransactionServiceImpl} предоставляет бизнес-логику для управления финансовыми операциями
 * с банковскими картами.
 * Он является реализацией интерфейса {@link com.example.bankcards.service.TransactionService}
 * и отвечает за перевод средств между картами, пополнение баланса карт и расчет общего баланса пользователя.
 * <p>
 * Сервис взаимодействует с репозиториями для доступа к данным карт и пользователей.
 * Для получения информации об аутентифицированном пользователе используется Spring Security.
 * </p>
 */
@Service
public class TransactionServiceImpl implements TransactionService {
    /**
     * Репозиторий для доступа к данным банковских карт.
     */
    private final BankCardsRepository bankCardsRepository;
    /**
     * Репозиторий для доступа к данным пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Конструктор для внедрения необходимых зависимостей.
     *
     * @param bankCardsRepository Репозиторий для доступа к данным банковских карт.
     * @param userRepository      Репозиторий для доступа к данным пользователей.
     */
    public TransactionServiceImpl(BankCardsRepository bankCardsRepository, UserRepository userRepository) {
        this.bankCardsRepository = bankCardsRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создает новую транзакцию, переводя средства между двумя банковскими картами.
     * <p>
     * Метод выполняет следующие проверки и действия:
     * <ol>
     *     <li>Проверяет, что карты отправителя и получателя не являются одной и той же картой.</li>
     *     <li>Находит карты по их идентификаторам, выбрасывая {@link NotFoundException}, если карта не найдена.</li>
     *     <li>Проверяет, что текущий аутентифицированный пользователь является владельцем обеих карт.</li>
     *     <li>Проверяет наличие достаточных средств на карте отправителя.</li>
     *     <li>Уменьшает баланс на карте отправителя и увеличивает на карте получателя на указанную сумму.</li>
     * </ol>
     * </p>
     *
     * @param transactionDto DTO, содержащий идентификаторы карты отправителя (fromCardId),
     *                       карты получателя (toCardId) и сумму перевода (amount).
     * @throws IllegalArgumentException Если карта-отправитель и карта-получатель совпадают,
     *                                  или на карте-отправителе недостаточно средств.
     * @throws NotFoundException        Если карта-отправитель или карта-получатель не найдена.
     * @throws BadCredentialsException  Если текущий аутентифицированный пользователь не является владельцем
     *                                  одной или обеих указанных карт.
     */
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

    /**
     * Рассчитывает и возвращает общий баланс по всем банковским картам, принадлежащим текущему аутентифицированному пользователю.
     * <p>
     * Если у пользователя нет карт или их общий баланс равен {@code null}, возвращается {@link BigDecimal#ZERO}.
     * </p>
     *
     * @return {@link TotalBalanceDto}, содержащий суммарный баланс всех карт пользователя.
     * @throws NotFoundException Если аутентифицированный пользователь не найден в системе.
     */
    @Override
    @Transactional(readOnly = true)
    public TotalBalanceDto calculateTotalBalanceForUser() {
        User currentUser = getAuthenticatedUser();
        BigDecimal totalBalance = bankCardsRepository.calculateTotalBalanceByUserId(currentUser.getId());

        BigDecimal result = Optional.ofNullable(totalBalance).orElse(BigDecimal.ZERO);

        return new TotalBalanceDto(result);
    }

    /**
     * Пополняет баланс указанной банковской карты.
     * <p>
     * Метод выполняет следующие проверки и действия:
     * <ol>
     *     <li>Находит карту по её идентификатору, выбрасывая {@link NotFoundException}, если карта не найдена.</li>
     *     <li>Проверяет, что текущий аутентифицированный пользователь является владельцем данной карты.</li>
     *     <li>Увеличивает баланс карты на указанную сумму.</li>
     * </ol>
     * </p>
     *
     * @param cardId     Идентификатор карты, которую необходимо пополнить.
     * @param depositDto DTO, содержащий сумму пополнения (amount).
     * @throws NotFoundException       Если карта с указанным ID не найдена.
     * @throws BadCredentialsException Если текущий аутентифицированный пользователь не является владельцем карты.
     */
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

    /**
     * Внутренний вспомогательный метод для получения объекта {@link User} текущего аутентифицированного пользователя.
     * Использует {@link SecurityContextHolder} для доступа к информации об аутентификации.
     *
     * @return Сущность {@link User} текущего аутентифицированного пользователя.
     * @throws NotFoundException Если аутентифицированный пользователь не найден в базе данных
     *                           (что может указывать на несоответствие между данными аутентификации и БД).
     */
    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Аутентифицированный пользователь " + username
                        + " не найден в системе"));
    }
}
