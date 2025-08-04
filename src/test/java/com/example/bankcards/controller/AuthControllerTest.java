package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.service.AuthenticationService;
import com.example.bankcards.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(auth ->
                            auth.requestMatchers("/api/v1/auth/login").permitAll()
                                    .anyRequest().authenticated()
                    )
                    .csrf(csrf -> {});
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private AuthRequest validRequest;
    private AuthRequest invalidRequest;

    @BeforeEach
    void setUp() {
        validRequest = new AuthRequest("testuser", "password");
        invalidRequest = new AuthRequest("wronguser", "wrongpassword");
    }

    @Test
    @DisplayName("POST /login должен вернуть 200 OK и токен при верных данных")
    void login_whenCredentialsAreValid_shouldReturnOkAndToken() throws Exception {
        AuthResponse expectedResponse = new AuthResponse("fake-jwt-token");

        when(authenticationService.login(any(AuthRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .with(csrf())) // <-- ВОТ ОН, КЛЮЧ К РЕШЕНИЮ 403
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(expectedResponse.token()));
    }

    @Test
    @DisplayName("POST /login должен вернуть 401 Unauthorized при неверных данных")
    void login_whenCredentialsAreInvalid_shouldReturnUnauthorized() throws Exception {
        when(authenticationService.login(any(AuthRequest.class)))
                .thenThrow(new BadCredentialsException("Неверные учетные данные"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf())) // <-- И ЗДЕСЬ ТОЖЕ
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /login должен вернуть 400 Bad Request при пустом теле запроса")
    void login_whenBodyIsMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /login должен вернуть 400 Bad Request при невалидном теле (пустой username)")
    void login_whenUsernameIsBlankInRequest_shouldReturnBadRequest() throws Exception {
        AuthRequest requestWithBlankUsername = new AuthRequest("", "password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithBlankUsername))
                        .with(csrf())) // <-- И ЗДЕСЬ
                .andExpect(status().isBadRequest());
    }
}