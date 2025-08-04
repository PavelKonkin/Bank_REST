package com.example.bankcards.service;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.BankCardSpecification;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.BankCardsServiceImpl;
import com.example.bankcards.util.BankCardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankCardsServiceImplTest {
    @Mock
    private BankCardSpecification bankCardSpecification;
    @Mock
    private BankCardsRepository bankCardsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BankCardMapper bankCardMapper;

    @Captor
    private ArgumentCaptor<BankCard> bankCardArgumentCaptor;

    @InjectMocks
    private BankCardsServiceImpl bankCardsService;

    private User testUser;
    private BankCard testCard;
    private CreateBankCardDto createDto;
    private BankCardDto cardDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bankCardsService, "hashPepper", "test-pepper");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        createDto = new CreateBankCardDto();
        createDto.setOwnerId(1L);
        createDto.setCardNumber("1234567812345678");

        testCard = new BankCard();
        testCard.setId(10L);
        testCard.setOwner(testUser);
        testCard.setCardNumberHash("someHash");
        testCard.setStatus(CardStatus.ACTIVE);

        cardDto = new BankCardDto();
        cardDto.setId(10L);
    }

    @Test
    @DisplayName("create должен успешно создавать карту, если пользователь существует")
    void create_whenUserExists_shouldSaveCard() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bankCardMapper.toEntity(createDto)).thenReturn(testCard);

        bankCardsService.create(createDto);

        verify(bankCardsRepository).save(bankCardArgumentCaptor.capture());
        BankCard savedCard = bankCardArgumentCaptor.getValue();

        assertThat(savedCard.getOwner()).isEqualTo(testUser);
        assertThat(savedCard.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedCard.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(savedCard.getCardNumberHash()).isNotNull();
        assertThat(savedCard.getCardNumberHash()).isNotEqualTo("someHash");
    }

    @Test
    @DisplayName("create должен бросать NotFoundException, если пользователь не найден")
    void create_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bankCardsService.create(createDto));
        verify(bankCardsRepository, never()).save(any());
    }

    @Test
    @DisplayName("create должен пробрасывать DataIntegrityViolationException при дубликате карты")
    void create_whenCardNumberExists_shouldThrowDataIntegrityViolationException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bankCardMapper.toEntity(createDto)).thenReturn(testCard);
        when(bankCardsRepository.save(any(BankCard.class)))
                .thenThrow(new DataIntegrityViolationException("Карта с таким номером уже существует."));

        assertThrows(DataIntegrityViolationException.class, () -> bankCardsService.create(createDto));
    }


    @Test
    @DisplayName("blockCard должен менять статус на BLOCKED")
    void blockCard_shouldChangeStatusToBlocked() {
        testCard.setStatus(CardStatus.ACTIVE);
        when(bankCardsRepository.findById(10L)).thenReturn(Optional.of(testCard));

        bankCardsService.blockCard(10L);

        verify(bankCardsRepository).save(bankCardArgumentCaptor.capture());
        assertThat(bankCardArgumentCaptor.getValue().getStatus()).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    @DisplayName("activateCard должен менять статус на ACTIVE")
    void activateCard_shouldChangeStatusToActive() {
        testCard.setStatus(CardStatus.BLOCKED);
        when(bankCardsRepository.findById(10L)).thenReturn(Optional.of(testCard));

        bankCardsService.activateCard(10L);

        verify(bankCardsRepository).save(bankCardArgumentCaptor.capture());
        assertThat(bankCardArgumentCaptor.getValue().getStatus()).isEqualTo(CardStatus.ACTIVE);
    }

    @Test
    @DisplayName("requestBlock должен блокировать карту, если запрос от владельца")
    void requestBlock_whenUserIsOwner_shouldBlockCard() {
        mockSecurityContext("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bankCardsRepository.findById(10L)).thenReturn(Optional.of(testCard));

        bankCardsService.requestBlock(10L);

        verify(bankCardsRepository).save(testCard);
        assertThat(testCard.getStatus()).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    @DisplayName("requestBlock должен бросать BadCredentialsException, если запрос не от владельца")
    void requestBlock_whenUserIsNotOwner_shouldThrowBadCredentialsException() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("anotherUser");

        mockSecurityContext("anotherUser");
        when(userRepository.findByUsername("anotherUser")).thenReturn(Optional.of(anotherUser));
        when(bankCardsRepository.findById(10L)).thenReturn(Optional.of(testCard));

        assertThrows(BadCredentialsException.class, () -> bankCardsService.requestBlock(10L));
        verify(bankCardsRepository, never()).save(any());
    }

    @Test
    @DisplayName("searchCurrentUserCards должен возвращать отфильтрованные карты текущего пользователя")
    void searchCurrentUserCards_shouldReturnFilteredCards() {
        mockSecurityContext("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Specification<BankCard> mockSpec = mock(Specification.class);
        when(bankCardSpecification.build(eq(1L), any(), any(), any())).thenReturn(mockSpec);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BankCard> cardPage = new PageImpl<>(Collections.singletonList(testCard), pageable, 1);
        when(bankCardsRepository.findAll(mockSpec, pageable)).thenReturn(cardPage);
        when(bankCardMapper.toDto(testCard)).thenReturn(cardDto);

        Page<BankCardDto> result = bankCardsService.searchCurrentUserCards(null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(10L);
        verify(bankCardSpecification).build(eq(1L), any(), any(), any());
        verify(bankCardsRepository).findAll(mockSpec, pageable);
    }

    private void mockSecurityContext(String username) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(username);
    }
}
