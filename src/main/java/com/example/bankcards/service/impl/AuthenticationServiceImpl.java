package com.example.bankcards.service.impl;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.AuthenticationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Сервис {@code AuthenticationServiceImpl} предоставляет функциональность для аутентификации пользователей в системе.
 * Он является реализацией интерфейса {@link com.example.bankcards.service.AuthenticationService} и отвечает
 * за процесс входа пользователя, включая проверку учетных данных и генерацию JWT в случае успешной аутентификации.
 * <p>
 * Этот сервис использует {@link AuthenticationManager} для проверки имени пользователя и пароля,
 * а также {@link JwtService} для создания токена доступа.
 * </p>
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    /**
     * Менеджер аутентификации Spring Security, используемый для проверки учетных данных пользователя.
     */
    private final AuthenticationManager authenticationManager;
    /**
     * Сервис для генерации JSON Web Tokens (JWT) после успешной аутентификации.
     */
    private final JwtService jwtService;

    /**
     * Конструктор для внедрения необходимых зависимостей.
     *
     * @param authenticationManager Менеджер аутентификации Spring Security, используемый для проверки учетных данных пользователя.
     * @param jwtService            Сервис для генерации JSON Web Tokens (JWT) после успешной аутентификации.
     */
    public AuthenticationServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Обрабатывает запрос на вход пользователя в систему.
     * <p>
     * Метод выполняет следующие шаги:
     * <ol>
     *     <li>Создает {@link UsernamePasswordAuthenticationToken} из имени пользователя и пароля.</li>
     *     <li>Передает токен в {@link AuthenticationManager} для аутентификации.
     *         Если учетные данные неверны, {@code AuthenticationManager} выбросит исключение
     *         (например, {@code BadCredentialsException}).</li>
     *     <li>В случае успешной аутентификации извлекает объект {@link UserDetails} из результата.</li>
     *     <li>Генерирует JSON Web Token (JWT) для аутентифицированного пользователя с помощью {@link JwtService}.</li>
     *     <li>Возвращает {@link AuthResponse}, содержащий сгенерированный JWT.</li>
     * </ol>
     * </p>
     *
     * @param loginRequest Объект {@link AuthRequest}, содержащий имя пользователя и пароль для аутентификации.
     * @return Объект {@link AuthResponse}, содержащий сгенерированный JWT в случае успешной аутентификации.
     * @throws org.springframework.security.core.AuthenticationException Если аутентификация не удалась (например, неверные учетные данные).
     */
    @Override
    public AuthResponse login(AuthRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        var user = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }
}
