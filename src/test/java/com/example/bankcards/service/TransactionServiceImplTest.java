package com.example.bankcards.service;

import com.example.bankcards.dto.CreateTransactionDto;
import com.example.bankcards.dto.DepositDto;
import com.example.bankcards.dto.TotalBalanceDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    @Mock
    private BankCardsRepository bankCardsRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User currentUser;
    private BankCard fromCard;
    private BankCard toCard;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        fromCard = new BankCard();
        fromCard.setId(10L);
        fromCard.setOwner(currentUser);
        fromCard.setBalance(new BigDecimal("1000.00"));

        toCard = new BankCard();
        toCard.setId(20L);
        toCard.setOwner(currentUser);
        toCard.setBalance(new BigDecimal("500.00"));
    }


    @Test
    @DisplayName("Успешное создание транзакции между картами одного пользователя")
    void createTransaction_whenSuccessful_shouldUpdateBalances() {
        mockSecurityContext(currentUser.getUsername());
        when(userRepository.findByUsername(currentUser.getUsername())).thenReturn(Optional.of(currentUser));
        when(bankCardsRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(bankCardsRepository.findById(20L)).thenReturn(Optional.of(toCard));

        CreateTransactionDto transactionDto = new CreateTransactionDto();
        transactionDto.setFromCardId(10L);
        transactionDto.setToCardId(20L);
        transactionDto.setAmount(new BigDecimal("100.00"));

        transactionService.createTransaction(transactionDto);

        assertThat(fromCard.getBalance()).isEqualByComparingTo("900.00");
        assertThat(toCard.getBalance()).isEqualByComparingTo("600.00");
    }

    @Test
    @DisplayName("createTransaction должен бросать исключение при переводе на ту же карту")
    void createTransaction_whenFromAndToCardAreSame_shouldThrowException() {
        mockSecurityContext(currentUser.getUsername());
        when(userRepository.findByUsername(currentUser.getUsername())).thenReturn(Optional.of(currentUser));
        CreateTransactionDto transactionDto = new CreateTransactionDto();
        transactionDto.setFromCardId(10L);
        transactionDto.setToCardId(10L);
        transactionDto.setAmount(new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(transactionDto));
        verify(bankCardsRepository, never()).findById(anyLong()); // Репозиторий карт не должен был вызываться
    }

    @Test
    @DisplayName("createTransaction должен бросать исключение при недостатке средств")
    void createTransaction_whenInsufficientFunds_shouldThrowException() {
        mockSecurityContext(currentUser.getUsername());
        when(userRepository.findByUsername(currentUser.getUsername())).thenReturn(Optional.of(currentUser));
        when(bankCardsRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(bankCardsRepository.findById(20L)).thenReturn(Optional.of(toCard));
        CreateTransactionDto transactionDto = new CreateTransactionDto();
        transactionDto.setFromCardId(10L);
        transactionDto.setToCardId(20L);
        transactionDto.setAmount(new BigDecimal("20000.00"));

        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(transactionDto));

        assertThat(fromCard.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(toCard.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("createTransaction должен бросать исключение, если пользователь не владелец карты-отправителя")
    void createTransaction_whenUserNotOwnerOfFromCard_shouldThrowException() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        fromCard.setOwner(anotherUser);

        mockSecurityContext(currentUser.getUsername());
        when(userRepository.findByUsername(currentUser.getUsername())).thenReturn(Optional.of(currentUser));
        when(bankCardsRepository.findById(10L)).thenReturn(Optional.of(fromCard));
        when(bankCardsRepository.findById(20L)).thenReturn(Optional.of(toCard));

        CreateTransactionDto transactionDto = new CreateTransactionDto();
        transactionDto.setFromCardId(10L);
        transactionDto.setToCardId(20L);
        transactionDto.setAmount(BigDecimal.TEN);

        assertThrows(BadCredentialsException.class, () -> transactionService.createTransaction(transactionDto));
    }

    @Test
    @DisplayName("Успешный расчет общего баланса пользователя")
    void calculateTotalBalanceForUser_whenUserHasCards_shouldReturnTotalBalance() {
        mockSecurityContext(currentUser.getUsername());
        when(userRepository.findByUsername(currentUser.getUsername())).thenReturn(Optional.of(currentUser));
        when(bankCardsRepository.calculateTotalBalanceByUserId(currentUser.getId()))
                .thenReturn(new BigDecimal("1500.00"));

        TotalBalanceDto result = transactionService.calculateTotalBalanceForUser();

        assertThat(result).isNotNull();
        assertThat(result.getTotalBalance()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("Расчет общего баланса должен вернуть 0, если у пользователя нет карт")
    void calculateTotalBalanceForUser_whenRepoReturnsNull_shouldReturnZero() {
        mockSecurityContext(currentUser.getUsername());
        when(userRepository.findByUsername(currentUser.getUsername())).thenReturn(Optional.of(currentUser));
        when(bankCardsRepository.calculateTotalBalanceByUserId(currentUser.getId())).thenReturn(null);

        TotalBalanceDto result = transactionService.calculateTotalBalanceForUser();

        assertThat(result).isNotNull();
        assertThat(result.getTotalBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Успешное пополнение баланса карты")
    void depositToCard_whenSuccessful_shouldIncreaseBalance() {
        mockSecurityContext(currentUser.getUsername());
        when(userRepository.findByUsername(currentUser.getUsername())).thenReturn(Optional.of(currentUser));
        when(bankCardsRepository.findById(10L)).thenReturn(Optional.of(fromCard));

        DepositDto depositDto = new DepositDto();
        depositDto.setAmount(new BigDecimal("250.50"));

        transactionService.depositToCard(10L, depositDto);

        assertThat(fromCard.getBalance()).isEqualByComparingTo("1250.50");
    }

    @Test
    @DisplayName("Пополнение баланса должно бросать исключение, если карта не найдена")
    void depositToCard_whenCardNotFound_shouldThrowNotFoundException() {
        when(bankCardsRepository.findById(99L)).thenReturn(Optional.empty());
        DepositDto depositDto = new DepositDto();
        depositDto.setAmount(BigDecimal.TEN);

        assertThrows(NotFoundException.class, () -> transactionService.depositToCard(99L, depositDto));
    }

    @Test
    @DisplayName("Пополнение баланса должно бросать исключение, если пользователь не владелец карты")
    void depositToCard_whenUserIsNotOwner_shouldThrowBadCredentialsException() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        fromCard.setOwner(anotherUser);

        mockSecurityContext(currentUser.getUsername());
        when(userRepository.findByUsername(currentUser.getUsername())).thenReturn(Optional.of(currentUser));
        when(bankCardsRepository.findById(10L)).thenReturn(Optional.of(fromCard));

        DepositDto depositDto = new DepositDto();
        depositDto.setAmount(BigDecimal.TEN);

        assertThrows(BadCredentialsException.class, () -> transactionService.depositToCard(10L, depositDto));
    }

    private void mockSecurityContext(String username) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
    }
}
