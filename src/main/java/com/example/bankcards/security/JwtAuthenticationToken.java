package com.example.bankcards.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

/**
 * Пользовательский объект аутентификации для JWT-авторизации в Spring Security.
 * <p>
 * Этот класс расширяет {@link AbstractAuthenticationToken} и используется для
 * представления аутентифицированного пользователя, чья личность была подтверждена
 * с помощью JSON Web Token (JWT).
 * </p>
 * <p>
 * В отличие от {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken},
 * который может использоваться как для запросов на аутентификацию, так и для
 * представления аутентифицированного субъекта, {@code JwtAuthenticationToken}
 * предназначен для использования *после* успешной валидации JWT.
 * </p>
 * <p>
 * Он содержит {@link UserDetails} в качестве принципала ({@code principal})
 * и сам JWT в качестве учетных данных ({@code credentials}).
 * </p>
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final UserDetails principal;
    private final String credentials;

    /**
     * Создает новый экземпляр {@code JwtAuthenticationToken} для аутентифицированного пользователя.
     * <p>
     * Этот конструктор используется, когда токен JWT уже был успешно проверен,
     * и пользователь считается аутентифицированным.
     * </p>
     *
     * @param principal Объект {@link UserDetails}, представляющий аутентифицированного пользователя.
     *                  Это "принципал" (субъект), связанный с данным токеном аутентификации.
     * @param credentials Строка, содержащая сам JWT, который был использован для аутентификации.
     *                    Это "учетные данные" (credentials).
     * @param authorities Коллекция полномочий (ролей) пользователя, которые определяют его права доступа.
     *                    Эти полномочия будут использоваться Spring Security для авторизации.
     */
    public JwtAuthenticationToken(UserDetails principal, String credentials,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true); // После проверки токена считаем, что пользователь аутентифицирован
    }

    /**
     * Возвращает учетные данные (credentials), связанные с этим токеном аутентификации.
     * <p>
     * В контексте JWT-аутентификации эти учетные данные представляют собой
     * сам необработанный JWT (строку токена).
     * </p>
     *
     * @return Строка, содержащая JWT.
     */
    @Override
    public Object getCredentials() {
        return credentials;
    }

    /**
     * Возвращает принципала (субъекта), связанного с этим токеном аутентификации.
     * <p>
     * В данном случае принципалом является объект {@link UserDetails},
     * который содержит подробную информацию об аутентифицированном пользователе.
     * </p>
     *
     * @return Объект {@link UserDetails}, представляющий аутентифицированного пользователя.
     */
    @Override
    public Object getPrincipal() {
        return principal;
    }

    /**
     * Сравнивает этот объект {@code JwtAuthenticationToken} с другим объектом
     * на равенство.
     * <p>
     * Два объекта {@code JwtAuthenticationToken} считаются равными, если
     * совпадают их принципал ({@link UserDetails}), учетные данные (JWT строка)
     * и поля базового класса {@link AbstractAuthenticationToken}.
     * </p>
     *
     * @param o Объект для сравнения.
     * @return {@code true}, если объекты равны; {@code false} в противном случае.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JwtAuthenticationToken that = (JwtAuthenticationToken) o;
        return Objects.equals(principal, that.principal) && Objects.equals(credentials, that.credentials);
    }

    /**
     * Вычисляет хэш-код для этого объекта {@code JwtAuthenticationToken}.
     * <p>
     * Хэш-код основан на хэш-кодах принципала ({@link UserDetails}),
     * учетных данных (JWT строка) и хэш-коде базового класса.
     * </p>
     *
     * @return Хэш-код объекта.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), principal, credentials);
    }
}
