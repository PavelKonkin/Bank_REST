package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CreateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Реализация сервиса {@link UserService}, предоставляющая бизнес-логику для управления пользователями.
 * Этот класс взаимодействует с {@link UserRepository} для доступа к данным,
 * {@link UserMapper} для преобразования объектов между DTO и сущностями,
 * и {@link PasswordEncoder} для хеширования паролей.
 */
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param userRepository Репозиторий для доступа к данным пользователей.
     * @param userMapper Маппер для преобразования между DTO и сущностями пользователей.
     * @param passwordEncoder Кодировщик паролей для хеширования.
     */
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Находит и возвращает список всех зарегистрированных пользователей.
     *
     * @return Список объектов {@link UserDto}, представляющих всех пользователей.
     */
    @Override
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    /**
     * Находит и возвращает пользователя по его уникальному идентификатору.
     *
     * @param id Уникальный идентификатор пользователя.
     * @return Объект {@link UserDto}, представляющий найденного пользователя.
     * @throws NotFoundException Если пользователь с указанным ID не найден.
     */
    @Override
    public UserDto findUserById(Long id) {
        return userMapper.toDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found")));
    }

    /**
     * Создает нового пользователя в системе. Пароль пользователя будет хеширован.
     *
     * @param createUserDto Объект {@link CreateUserDto}, содержащий данные для создания нового пользователя.
     */
    @Override
    public void createUser(CreateUserDto createUserDto) {
        User user = userMapper.toEntity(createUserDto, passwordEncoder);
        userRepository.save(user);
    }

    /**
     * Удаляет пользователя из системы по его уникальному идентификатору.
     *
     * @param id Уникальный идентификатор пользователя, которого необходимо удалить.
     */
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
