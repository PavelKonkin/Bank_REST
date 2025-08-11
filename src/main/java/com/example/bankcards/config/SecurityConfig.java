package com.example.bankcards.config;

import com.example.bankcards.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурационный класс для Spring Security.
 * <p>
 * Включает веб-безопасность (@EnableWebSecurity) и безопасность на уровне методов (@EnableMethodSecurity).
 * Этот класс настраивает цепочку фильтров безопасности HTTP, интегрирует JWT-аутентификацию,
 * определяет правила доступа к URL-адресам и предоставляет бин для кодирования паролей.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Конструктор для {@code SecurityConfig}.
     * <p>
     * Внедряет {@link JwtAuthenticationFilter}, который будет использоваться в цепочке безопасности
     * для обработки JWT токенов из входящих запросов.
     * </p>
     * @param jwtAuthFilter Экземпляр {@link JwtAuthenticationFilter}, отвечающий за извлечение и валидацию JWT.
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Конфигурирует цепочку фильтров безопасности HTTP.
     * <p>
     * Этот метод определяет правила доступа к различным URL-адресам, управляет состоянием сессий
     * и интегрирует JWT-аутентификацию в процесс обработки запросов.
     * </p>
     * <ul>
     *     <li>Отключает CSRF-защиту, так как приложение использует токен-основанную аутентификацию (JWT).</li>
     *     <li>Разрешает доступ без аутентификации к конечным точкам аутентификации (логин) и документации OpenAPI/Swagger UI.</li>
     *     <li>Требует аутентификации для всех остальных запросов.</li>
     *     <li>Устанавливает политику управления сессиями как {@code STATELESS}, что соответствует
     *         бессерверной природе JWT-аутентификации (токен содержит всю необходимую информацию, сессии на сервере не хранятся).</li>
     *     <li>Добавляет {@link JwtAuthenticationFilter} перед {@link UsernamePasswordAuthenticationFilter}
     *         для обработки JWT токенов перед стандартной аутентификацией по имени пользователя/паролю.</li>
     * </ul>
     *
     * @param http Объект {@link HttpSecurity} для настройки безопасности HTTP.
     * @return Сконфигурированная {@link SecurityFilterChain}.
     * @throws Exception Если возникает ошибка при настройке безопасности.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/v3/api-docs/**",     // Разрешаем доступ к спецификации
                                "/v3/api-docs.yaml/**",     // Разрешаем доступ к спецификации
                                "/swagger-ui/**",
                                "/swagger-ui.html"// Разрешаем доступ к интерфейсу Swagger UI
                        ).permitAll()

                        // Все остальные запросы по-прежнему требуют аутентификации
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Предоставляет бин {@link AuthenticationManager}.
     * <p>
     * {@link AuthenticationManager} используется для обработки запросов на аутентификацию,
     * например, при входе пользователя в систему.
     * </p>
     * @param config Конфигурация аутентификации, используемая для получения менеджера.
     * @return Экземпляр {@link AuthenticationManager}.
     * @throws Exception Если не удается получить менеджер аутентификации.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Предоставляет бин {@link PasswordEncoder}.
     * <p>
     * Используется для безопасного кодирования (хеширования) и проверки паролей пользователей.
     * В данном случае используется {@link BCryptPasswordEncoder}, который является рекомендуемым
     * алгоритмом для хеширования паролей.
     * </p>
     * @return Экземпляр {@link PasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
