package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateTransactionDto;
import com.example.bankcards.dto.DepositDto;
import com.example.bankcards.dto.TotalBalanceDto;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@DisplayName("Тесты для TransactionController")
@WithMockUser(username = "testuser")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /transactions - Успешное создание транзакции")
    void createTransaction_withValidData_shouldSucceed() throws Exception {
        // Arrange
        CreateTransactionDto transactionDto = new CreateTransactionDto();
        transactionDto.setFromCardId(1L);
        transactionDto.setToCardId(2L);
        transactionDto.setAmount(new BigDecimal("100.00"));

        doNothing().when(transactionService).createTransaction(any(CreateTransactionDto.class));

        mockMvc.perform(post("/api/v1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDto)))
                .andExpect(status().isOk());

        verify(transactionService).createTransaction(any(CreateTransactionDto.class));
    }

    @Test
    @DisplayName("POST /transactions - Ошибка 403, если пользователь не владелец карты-отправителя")
    void createTransaction_whenNotOwner_shouldReturnForbidden() throws Exception {
        CreateTransactionDto transactionDto = new CreateTransactionDto();
        transactionDto.setFromCardId(5L);
        transactionDto.setToCardId(2L);
        transactionDto.setAmount(new BigDecimal("50.00"));

        doThrow(new BadCredentialsException("User is not the owner of the source card"))
                .when(transactionService).createTransaction(any(CreateTransactionDto.class));

        mockMvc.perform(post("/api/v1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /transactions - Ошибка 400 при невалидных данных (например, отрицательная сумма)")
    void createTransaction_withInvalidAmount_shouldReturnBadRequest() throws Exception {
        CreateTransactionDto transactionDto = new CreateTransactionDto();
        transactionDto.setFromCardId(1L);
        transactionDto.setToCardId(2L);
        transactionDto.setAmount(new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/v1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /balance - Успешное получение общего баланса пользователя")
    void getTotalBalance_shouldReturnTotalBalance() throws Exception {
        TotalBalanceDto balanceDto = new TotalBalanceDto(new BigDecimal("12345.67"));
        given(transactionService.calculateTotalBalanceForUser()).willReturn(balanceDto);

        mockMvc.perform(get("/api/v1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBalance", is(12345.67)));

        verify(transactionService).calculateTotalBalanceForUser();
    }

    @Test
    @DisplayName("POST /cards/{id}/deposit - Успешное пополнение карты")
    void depositToCard_withValidData_shouldSucceed() throws Exception {
        Long cardId = 1L;
        DepositDto depositDto = new DepositDto();
        depositDto.setAmount(new BigDecimal("500.00"));

        doNothing().when(transactionService).depositToCard(eq(cardId), any(DepositDto.class));

        mockMvc.perform(post("/api/v1/cards/{id}/deposit", cardId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositDto)))
                .andExpect(status().isOk());

        verify(transactionService).depositToCard(eq(cardId), any(DepositDto.class));
    }

    @Test
    @DisplayName("POST /cards/{id}/deposit - Ошибка 403 при пополнении чужой карты")
    void depositToCard_whenNotOwner_shouldReturnForbidden() throws Exception {
        Long cardId = 2L;
        DepositDto depositDto = new DepositDto();
        depositDto.setAmount(new BigDecimal("500.00"));

        doThrow(new BadCredentialsException("User is not the owner of this card"))
                .when(transactionService).depositToCard(eq(cardId), any(DepositDto.class));

        mockMvc.perform(post("/api/v1/cards/{id}/deposit", cardId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /cards/{id}/deposit - Ошибка 404 если карта не найдена")
    void depositToCard_whenCardNotFound_shouldReturnNotFound() throws Exception {
        Long cardId = 99L;
        DepositDto depositDto = new DepositDto();
        depositDto.setAmount(new BigDecimal("100.00"));

        doThrow(new NotFoundException("Card not found"))
                .when(transactionService).depositToCard(eq(cardId), any(DepositDto.class));

        mockMvc.perform(post("/api/v1/cards/{id}/deposit", cardId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositDto)))
                .andExpect(status().isNotFound());
    }
}