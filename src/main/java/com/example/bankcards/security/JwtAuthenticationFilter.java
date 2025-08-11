package com.example.bankcards.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр Spring Security для аутентификации запросов с использованием JSON Web Token (JWT).
 * <p>
 * Этот класс расширяет {@link OncePerRequestFilter}, что гарантирует его выполнение
 * ровно один раз для каждого входящего HTTP-запроса.
 * </p>
 * <p>
 * Основная задача фильтра — перехватывать запросы, проверять наличие и валидность JWT
 * в заголовке "Authorization" (с префиксом "Bearer "),
 * извлекать из него имя пользователя, загружать соответствующие данные пользователя
 * и устанавливать объект аутентификации в {@link SecurityContextHolder},
 * если токен действителен.
 * </p>
 * <p>
 * Аннотация {@code @Component} делает этот класс управляемым компонентом Spring,
 * а {@code @Slf4j} предоставляет функциональность логирования Lombok.
 * </p>
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Конструктор для внедрения зависимостей {@link JwtService} и {@link UserDetailsService}.
     *
     * @param jwtService Сервис, отвечающий за генерацию, валидацию и извлечение данных из JWT.
     * @param userDetailsService Сервис для загрузки информации о пользователе по его имени из хранилища.
     */
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Выполняет логику фильтрации для каждого внутреннего HTTP-запроса.
     * <p>
     * Этот метод:
     * <ol>
     *     <li>Извлекает заголовок "Authorization" из запроса.</li>
     *     <li>Если заголовок отсутствует или не начинается с "Bearer ",
     *         пропускает запрос дальше по цепочке фильтров и завершает работу.</li>
     *     <li>Извлекает JWT (токен) и имя пользователя из него.</li>
     *     <li>Если имя пользователя успешно извлечено и текущий контекст безопасности
     *         еще не содержит аутентификации:
     *         <ul>
     *             <li>Загружает {@link UserDetails} для найденного имени пользователя.</li>
     *             <li>Проверяет валидность JWT с использованием загруженных данных пользователя.</li>
     *             <li>Если токен валиден, создает объект {@link UsernamePasswordAuthenticationToken}
     *                 и устанавливает его в {@link SecurityContextHolder}, тем самым аутентифицируя запрос.</li>
     *         </ul>
     *     </li>
     *     <li>Продолжает выполнение цепочки фильтров, передавая запрос следующему фильтру.</li>
     * </ol>
     * </p>
     *
     * @param request Объект {@link HttpServletRequest}, представляющий входящий HTTP-запрос.
     * @param response Объект {@link HttpServletResponse}, представляющий HTTP-ответ.
     * @param filterChain Объект {@link FilterChain}, представляющий цепочку фильтров.
     * @throws ServletException Если возникает ошибка, связанная с сервлетом.
     * @throws IOException Если возникает ошибка ввода-вывода.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);

    }
}
