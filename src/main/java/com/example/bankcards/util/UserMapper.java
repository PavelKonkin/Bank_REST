package com.example.bankcards.util;

import com.example.bankcards.dto.CreateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <p>Интерфейс-маппер для преобразования между сущностями {@link User} и их DTO-представлениями.</p>
 *
 * <p>Этот маппер используется для преобразования объектов {@link User} в {@link UserDto} (для вывода)
 * и {@link CreateUserDto} в {@link User} (для создания/обновления). Он использует MapStruct
 * для автоматической генерации кода маппинга и интегрирован со Spring, что позволяет внедрять
 * его как компонент.</p>
 *
 * <p>Также обрабатывает кодирование паролей при преобразовании из DTO в сущность,
 * используя {@link PasswordEncoder}, который внедряется из контекста Spring.</p>
 *
 * @see User
 * @see UserDto
 * @see CreateUserDto
 * @see UserRole
 * @see PasswordEncoder
 * @see Mapper
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    /**
     * <p>Преобразует сущность {@link User} в объект передачи данных {@link UserDto}.</p>
     *
     * <p>Поле {@code role} преобразуется из {@link UserRole} в {@code String}
     * с помощью {@link #roleEntityToRoleName(UserRole)}.</p>
     *
     * @param user Сущность пользователя для преобразования.
     * @return Объект {@link UserDto}, представляющий пользователя.
     */
    @Mapping(source = "role", target = "role", qualifiedByName = "roleEntityToRoleName")
    UserDto toDto(User user);

    /**
     * <p>Преобразует объект {@link CreateUserDto} в сущность {@link User}.</p>
     *
     * <p>Поле {@code role} преобразуется из {@code String} в {@link UserRole}
     * с помощью {@link #roleNameToRoleEntity(String)}.
     * Поле {@code password} хешируется с помощью {@link #encodePassword(String, PasswordEncoder)}
     * и переданного {@link PasswordEncoder}.</p>
     *
     * @param createUserDto   DTO для создания нового пользователя.
     * @param passwordEncoder Кодировщик паролей {@link PasswordEncoder}, который будет использован
     *                        для хеширования пароля. Внедряется MapStruct из контекста Spring.
     * @return Сущность {@link User}, готовая к сохранению.
     */
    @Mapping(source = "role", target = "role", qualifiedByName = "roleNameToRoleEntity")
    @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword")
    User toEntity(CreateUserDto createUserDto, @Context PasswordEncoder passwordEncoder);

    /**
     * <p>Преобразует перечисление {@link UserRole} в его строковое имя.</p>
     *
     * <p>Метод помечен {@code @Named("roleEntityToRoleName")} для использования в аннотациях {@code @Mapping}.</p>
     *
     * @param role Перечисление роли пользователя.
     * @return Строковое представление имени роли (например, "ROLE_ADMIN"),
     *         или {@code null}, если входное значение {@code null}.
     */
    @Named("roleEntityToRoleName")
    default String roleEntityToRoleName(UserRole role) {
        if (role == null) {
            return null;
        }
        return role.name();
    }

    /**
     * <p>Преобразует строковое имя роли в соответствующее перечисление {@link UserRole}.</p>
     *
     * <p>Если {@code roleName} равно {@code null}, по умолчанию возвращается {@link UserRole#ROLE_USER}.</p>
     *
     * <p>Метод помечен {@code @Named("roleNameToRoleEntity")} для использования в аннотациях {@code @Mapping}.</p>
     *
     * @param roleName Строковое имя роли пользователя (например, "ROLE_ADMIN").
     * @return Перечисление {@link UserRole}, соответствующее имени,
     *         или {@link UserRole#ROLE_USER}, если входное значение {@code null}.
     * @throws IllegalArgumentException Если {@code roleName} не соответствует ни одной из констант {@link UserRole}.
     */
    @Named("roleNameToRoleEntity")
    default UserRole roleNameToRoleEntity(String roleName) {
        if (roleName == null) {
            return UserRole.ROLE_USER;
        }
        return UserRole.valueOf(roleName);
    }

    /**
     * <p>Кодирует (хеширует) предоставленный пароль с помощью {@link PasswordEncoder}.</p>
     *
     * <p>Этот метод используется для обеспечения безопасности хранения паролей.
     * Параметр {@code passwordEncoder} внедряется MapStruct из контекста Spring
     * благодаря аннотации {@code @Context}.</p>
     *
     * <p>Метод помечен {@code @Named("encodePassword")} для использования в аннотациях {@code @Mapping}.</p>
     *
     * @param password        Пароль в открытом виде для кодирования.
     * @param passwordEncoder Экземпляр {@link PasswordEncoder}, используемый для выполнения кодирования.
     * @return Закодированный (хешированный) пароль,
     *         или {@code null}, если входной пароль или кодировщик равны {@code null}.
     */
    @Named("encodePassword")
    default String encodePassword(String password, @Context PasswordEncoder passwordEncoder) {
        if (password == null || passwordEncoder == null) {
            return null;
        }
        return passwordEncoder.encode(password);
    }
}
