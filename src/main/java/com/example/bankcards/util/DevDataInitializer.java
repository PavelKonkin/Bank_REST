package com.example.bankcards.util;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * <p>Инициализатор данных для разработки, который автоматически создает
 * предопределенных пользователей в базе данных при запуске приложения,
 * если они еще не существуют.</p>
 *
 * <p>Этот компонент активируется только при активном профиле Spring {@code "dev"}.
 * Он предназначен для удобства разработки и тестирования, предоставляя
 * стандартные учетные записи для быстрого начала работы.</p>
 *
 * <p>Создает следующих пользователей:</p>
 * <ul>
 *     <li>Пользователь с именем {@code "admin"} и ролью {@code ROLE_ADMIN}.</li>
 *     <li>Пользователь с именем {@code "user"} и ролью {@code ROLE_USER}.</li>
 * </ul>
 *
 * <p>Пароль для обоих пользователей по умолчанию устанавливается как {@code "password"},
 * который хешируется с помощью {@link PasswordEncoder}.</p>
 *
 * <p>Является реализацией {@link ApplicationRunner}, что означает, что метод {@code run}
 * будет выполнен один раз после полного запуска контекста Spring Boot.</p>
 *
 * @see User
 * @see UserRepository
 * @see PasswordEncoder
 * @see ApplicationRunner
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements ApplicationRunner {
    /**
     * Репозиторий для доступа к данным пользователей и их сохранения.
     */
    private final UserRepository userRepository;
    /**
     * Кодировщик паролей, используемый для хеширования паролей перед сохранением их в базе данных.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Выполняется при старте приложения (только для профиля "dev").
     * Проверяет наличие пользователей "admin" и "user" в базе данных
     * и создает их, если они отсутствуют.
     *
     * <p>Пароль для создаваемых пользователей устанавливается как "password"
     * и хешируется с помощью {@link PasswordEncoder}.</p>
     *
     * <p>Ход выполнения:</p>
     * <ol>
     *     <li>Проверяет, существует ли пользователь "admin". Если нет, создает его с ролью {@code ROLE_ADMIN}.</li>
     *     <li>Проверяет, существует ли пользователь "user". Если нет, создает его с ролью {@code ROLE_USER}.</li>
     *     <li>Логирует информацию о создании или существовании каждого пользователя.</li>
     * </ol>
     *
     * @param args Аргументы командной строки, переданные приложению (не используются в данной реализации).
     * @throws Exception Если произошла ошибка во время инициализации данных.
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            log.info("No admin user found. Creating test admin...");
            var admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRole(UserRole.ROLE_ADMIN);
            userRepository.save(admin);
            log.info("Test admin created successfully.");
        } else {
            log.info("Test admin already exist.");
        }

        if (userRepository.findByUsername("user").isEmpty()) {
            log.info("No user found. Creating test user...");
            var user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("password"));
            user.setRole(UserRole.ROLE_USER);
            userRepository.save(user);
            log.info("Test user created successfully.");
        } else {
            log.info("Test user already exist.");
        }
    }
}
