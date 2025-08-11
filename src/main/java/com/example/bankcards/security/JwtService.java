package com.example.bankcards.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Сервис {@code JwtService} предоставляет функциональность для работы с JSON Web Tokens (JWT).
 * Он отвечает за генерацию, валидацию и извлечение информации из JWT.
 * <p>
 * Этот сервис используется для аутентификации и авторизации пользователей в системе,
 * позволяя безопасно передавать информацию о пользователе между клиентом и сервером.
 * </p>
 * <p>
 * <b>Важное замечание:</b> Секретный ключ ({@code secretKey}) должен быть достаточно длинным
 * (рекомендуется не менее 256 бит для HMAC SHA-256) и храниться в безопасном месте
 * (например, в файле конфигурации или переменных окружения)
 * </p>
 */
@Service
public class JwtService {
    /**
     * Секретный ключ, используемый для подписи и верификации JWT.
     * Значение загружается из свойства {@code jwt.service.secret} в файле конфигурации.
     */
    @Value("${jwt.service.secret}")
    private String secretKey;

    /**
     * Время жизни JWT в миллисекундах.
     * Определяет, как долго токен будет считаться действительным с момента его выдачи.
     * Значение загружается из свойства {@code jwt.service.expiration} в файле конфигурации.
     */
    @Value("${jwt.service.expiration}")
    private long jwtExpiration;

    /**
     * Извлекает имя пользователя (субъект) из JWT.
     *
     * @param token JWT-строка, из которой необходимо извлечь имя пользователя.
     * @return Имя пользователя (субъект) из токена.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Универсальный метод для извлечения конкретного утверждения (claim) из JWT.
     *
     * @param token          JWT-строка, из которой необходимо извлечь утверждение.
     * @param claimsResolver Функция, определяющая, какое утверждение нужно извлечь (например, {@code Claims::getSubject} для имени пользователя, {@code Claims::getExpiration} для даты истечения).
     * @param <T>            Тип возвращаемого утверждения.
     * @return Значение указанного утверждения.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Генерирует JWT для указанного пользователя без дополнительных утверждений.
     * Токен будет содержать имя пользователя (субъект), время выдачи и время истечения.
     *
     * @param userDetails Объект {@link UserDetails}, содержащий информацию о пользователе, для которого генерируется токен.
     * @return Сгенерированная JWT-строка.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Генерирует JWT для указанного пользователя с возможностью добавления дополнительных утверждений.
     * Токен будет содержать имя пользователя (субъект), время выдачи, время истечения
     * и любые переданные дополнительные утверждения.
     *
     * @param extraClaims Дополнительные утверждения (claims), которые будут включены в токен.
     * @param userDetails Объект {@link UserDetails}, содержащий информацию о пользователе, для которого генерируется токен.
     * @return Сгенерированная JWT-строка.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Проверяет валидность JWT для указанного пользователя.
     * Проверка включает сравнение имени пользователя из токена с именем пользователя из {@code UserDetails}
     * и проверку срока действия токена.
     *
     * @param token       JWT-строка для проверки.
     * @param userDetails Объект {@link UserDetails}, с которым сравнивается токен.
     * @return {@code true}, если токен действителен для данного пользователя и не истек; {@code false} в противном случае.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Проверяет, истек ли срок действия JWT.
     *
     * @param token JWT-строка для проверки.
     * @return {@code true}, если срок действия токена истек; {@code false} в противном случае.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Извлекает дату и время истечения срока действия из JWT.
     *
     * @param token JWT-строка, из которой необходимо извлечь дату истечения.
     * @return Объект {@link Date}, представляющий дату и время истечения срока действия токена.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлекает все утверждения (claims) из подписанного JWT.
     * Этот метод выполняет парсинг и верификацию токена с использованием секретного ключа.
     *
     * @param token JWT-строка, из которой необходимо извлечь утверждения.
     * @return Объект {@link Claims}, содержащий все утверждения из токена.
     * @throws io.jsonwebtoken.JwtException Если токен невалиден, поврежден или подпись не соответствует.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Возвращает секретный ключ, используемый для подписи и верификации JWT.
     * Ключ генерируется из {@code secretKey}, который декодируется из Base64.
     *
     * @return Объект {@link SecretKey} для подписи/верификации.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
