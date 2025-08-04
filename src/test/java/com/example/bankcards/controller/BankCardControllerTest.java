package com.example.bankcards.controller;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardDto;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.BankCardsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(BankCardController.class)
@DisplayName("Тесты для BankCardController")
class BankCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankCardsService bankCardsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private BankCardDto cardDto;

    @BeforeEach
    void setUp() {
        cardDto = new BankCardDto();
        cardDto.setMaskedCardNumber("4750657776370372");
    }

    @Nested
    @DisplayName("Тесты для эндпоинтов администратора (ADMIN)")
    class AdminFlowTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /api/v1/bankcards - Успешное создание карты")
        void createCard_asAdmin_shouldReturnCreated() throws Exception {
            CreateBankCardDto createDto = new CreateBankCardDto();
            createDto.setCardNumber("4750657776370372");
            createDto.setOwnerId(1L);
            createDto.setExpiryDate("12/12");

            doNothing().when(bankCardsService).create(any(CreateBankCardDto.class));

            mockMvc.perform(post("/api/v1/bankcards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated());

            verify(bankCardsService).create(any(CreateBankCardDto.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /api/v1/bankcards - Получение всех карт с пагинацией")
        void getAllCards_asAdmin_shouldReturnPageOfCards() throws Exception {
            Page<BankCardDto> cardPage = new PageImpl<>(List.of(cardDto));
            given(bankCardsService.getAllCards(any(Pageable.class))).willReturn(cardPage);

            mockMvc.perform(get("/api/v1/bankcards")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("PATCH /api/v1/bankcards/block/{id} - Успешная блокировка карты")
        void blockCard_asAdmin_shouldSucceed() throws Exception {
            doNothing().when(bankCardsService).blockCard(1L);

            mockMvc.perform(patch("/api/v1/bankcards/block/1").with(csrf()))
                    .andExpect(status().isOk());

            verify(bankCardsService).blockCard(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("PATCH /api/v1/bankcards/activate/{id} - Успешная активация карты")
        void activateCard_asAdmin_shouldSucceed() throws Exception {
            doNothing().when(bankCardsService).activateCard(1L);

            mockMvc.perform(patch("/api/v1/bankcards/activate/1").with(csrf()))
                    .andExpect(status().isOk());

            verify(bankCardsService).activateCard(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("DELETE /api/v1/bankcards/{id} - Успешное удаление карты")
        void deleteCard_asAdmin_shouldSucceed() throws Exception {
            doNothing().when(bankCardsService).delete(1L);

            mockMvc.perform(delete("/api/v1/bankcards/1").with(csrf()))
                    .andExpect(status().isNoContent());

            verify(bankCardsService).delete(1L);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("POST /api/v1/bankcards - Доступ запрещен для роли USER")
        void createCard_asUser_shouldBeForbidden() throws Exception {
            CreateBankCardDto createDto = new CreateBankCardDto();

            mockMvc.perform(post("/api/v1/bankcards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Тесты для эндпоинтов пользователя (USER)")
    class UserFlowTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("GET /api/v1/bankcards/my - Получение своих карт")
        void getCurrentUserCards_asUser_shouldReturnOwnCards() throws Exception {
            Page<BankCardDto> cardPage = new PageImpl<>(List.of(cardDto));
            given(bankCardsService.searchCurrentUserCards(any(), any(), any(), any(Pageable.class)))
                    .willReturn(cardPage);

            mockMvc.perform(get("/api/v1/bankcards/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));

            verify(bankCardsService).searchCurrentUserCards(eq(null), eq(null), eq(null), any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("PATCH /api/v1/bankcards/my/block/{id} - Успешная блокировка своей карты")
        void requestBlock_asUser_shouldSucceed() throws Exception {
            doNothing().when(bankCardsService).requestBlock(1L);

            mockMvc.perform(patch("/api/v1/bankcards/my/block/1").with(csrf()))
                    .andExpect(status().isOk());

            verify(bankCardsService).requestBlock(1L);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("PATCH /api/v1/bankcards/my/block/{id} - Запрет на блокировку чужой карты")
        void requestBlock_forAnotherUserCard_shouldBeForbidden() throws Exception {
            doThrow(new BadCredentialsException("Access is denied")).when(bankCardsService).requestBlock(2L);

            mockMvc.perform(patch("/api/v1/bankcards/my/block/2").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /api/v1/bankcards/my - Доступ запрещен для роли ADMIN")
        void getCurrentUserCards_asAdmin_shouldBeForbidden() throws Exception {
            mockMvc.perform(get("/api/v1/bankcards/my"))
                    .andExpect(status().isForbidden());
        }
    }
}