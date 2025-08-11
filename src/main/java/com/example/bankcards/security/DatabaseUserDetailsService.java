package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Пользовательская реализация {@link UserDetailsService},
 * предназначенная для загрузки данных пользователя из базы данных.
 * <p>
 * Этот класс интегрирует сущности {@link User}
 * с механизмом аутентификации Spring Security.
 * </p>
 * <p>
 * Аннотация {@code @Primary} указывает, что это предпочтительная реализация
 * {@link UserDetailsService}, если в контексте приложения существует несколько.
 * </p>
 */
@Service
@Primary
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Конструктор для внедрения зависимости {@link UserRepository}.
     *
     * @param userRepository Репозиторий для доступа к данным пользователей.
     */
    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Загружает данные пользователя по его имени пользователя (username).
     * <p>
     * Этот метод вызывается фреймворком Spring Security во время процесса аутентификации
     * для получения информации о пользователе из настроенного источника данных (в данном случае, базы данных).
     * </p>
     * <p>
     * Он находит {@link User} по имени пользователя,
     * преобразует его роль в {@link GrantedAuthority}
     * и возвращает объект {@link UserDetails},
     * который используется Spring Security для проверки учетных данных и авторизации.
     * </p>
     *
     * @param username Имя пользователя, по которому осуществляется поиск.
     * @return Объект {@link UserDetails},
     *         содержащий имя пользователя, хешированный пароль и список его полномочий (ролей).
     * @throws UsernameNotFoundException если пользователь с указанным именем не найден в базе данных.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь '" + username + "' не найден"));

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
