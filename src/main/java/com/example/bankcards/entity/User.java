package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * Представляет сущность пользователя в системе банковских карт.
 * Этот класс маппится на таблицу "users" в базе данных и содержит основные данные пользователя,
 * такие как уникальный идентификатор, имя пользователя, пароль и роль.
 * Используется для аутентификации и авторизации пользователей.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class User {
    /**
     * Уникальный идентификатор пользователя.
     * Генерируется автоматически базой данных при создании новой записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    /**
     * Уникальное имя пользователя.
     * Используется как логин для аутентификации в системе.
     */
    @Column(unique = true)
    private String username;

    /**
     * Пароль пользователя.
     * Хранится в зашифрованном (хешированном) виде.
     */
    @Column
    private String password;

    /**
     * Роль пользователя в системе.
     * Определяет права доступа и возможности пользователя.
     * Значение хранится в базе данных как строка, соответствующая элементу перечисления {@link UserRole}.
     */
    @Enumerated(EnumType.STRING)
    @Column
    private UserRole role;

    /**
     * Конструктор для создания нового объекта User с указанным именем пользователя и паролем.
     * Идентификатор и роль будут установлены позже или по умолчанию.
     *
     * @param username Имя пользователя.
     * @param password Пароль пользователя.
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Сравнивает текущий объект User с другим объектом на равенство.
     * Два объекта User считаются равными, если их идентификаторы, имена пользователя, пароли
     * и роли совпадают.
     *
     * @param o Объект для сравнения.
     * @return {@code true}, если объекты равны; {@code false} в противном случае.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username)
                && Objects.equals(password, user.password) && role == user.role;
    }

    /**
     * Вычисляет хеш-код для объекта User.
     * Хеш-код основан на значениях идентификатора, имени пользователя, пароля и роли.
     *
     * @return Хеш-код объекта.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, role);
    }
}
