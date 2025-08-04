package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    // Вспомогательная переменная для тестов
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        testUserDetails = new User("testuser", "password", new ArrayList<>());
    }

    @Test
    @DisplayName("Успешный логин должен возвращать AuthResponse с токеном")
    void login_whenCredentialsAreCorrect_shouldReturnAuthResponse() {
        AuthRequest loginRequest = new AuthRequest("testuser", "password");
        String expectedToken = "fake-jwt-token";

        Authentication mockedAuthentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockedAuthentication);

        when(mockedAuthentication.getPrincipal()).thenReturn(testUserDetails);

        when(jwtService.generateToken(testUserDetails)).thenReturn(expectedToken);

        AuthResponse response = authenticationService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(expectedToken);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(testUserDetails);
    }

    @Test
    @DisplayName("Логин с неверными данными должен бросать BadCredentialsException")
    void login_whenCredentialsAreIncorrect_shouldThrowBadCredentialsException() {
        AuthRequest loginRequest = new AuthRequest("wronguser", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Неверные учетные данные"));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.login(loginRequest)
        );

        assertThat(exception.getMessage()).isEqualTo("Неверные учетные данные");

        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }
}
