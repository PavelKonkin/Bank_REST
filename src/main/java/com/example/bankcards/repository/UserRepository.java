package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Интерфейс репозитория Spring Data JPA для сущности {@link User}.
 * <p>
 * Предоставляет стандартные операции CRUD (создание, чтение, обновление, удаление)
 * для пользователей.
 * <p>
 * Используется для взаимодействия с базой данных и управления сущностями {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Находит пользователя по его имени пользователя (username).
     * <p>
     * Spring Data JPA автоматически генерирует реализацию этого метода
     * на основе соглашений об именовании методов.
     * </p>
     * @param username Имя пользователя, по которому осуществляется поиск.
     * @return {@link Optional}, содержащий найденного пользователя, если он существует,
     *         или пустой {@link Optional}, если пользователь с таким именем не найден.
     */
    Optional<User> findByUsername(String username);
}
