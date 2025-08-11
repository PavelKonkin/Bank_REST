package com.example.bankcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * <p>Основной класс приложения "Bank Cards".</p>
 *
 * <p>Этот класс является точкой входа для Spring Boot приложения.
 * Он конфигурирует ключевые аспекты приложения, такие как автоматическая настройка,
 * безопасность на уровне методов и поддержка Spring Data в веб-слое.</p>
 *
 * <h3>Ключевые аннотации:</h3>
 * <ul>
 *     <li>{@link org.springframework.boot.autoconfigure.SpringBootApplication @SpringBootApplication}:
 *         Объединяет в себе {@code @Configuration}, {@code @EnableAutoConfiguration} и {@code @ComponentScan}.
 *         Это базовая аннотация для любого Spring Boot приложения, которая обеспечивает
 *         автоматическую настройку Spring и сканирование компонентов в текущем пакете
 *         и его подпакетах.</li>
 *     <li>{@link org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity @EnableMethodSecurity}:
 *         Активирует безопасность на уровне методов в Spring Security. Это позволяет
 *         использовать аннотации, такие как {@code @PreAuthorize}, {@code @PostAuthorize},
 *         {@code @Secured} и {@code @RolesAllowed}, непосредственно на методах сервисов
 *         или контроллеров для контроля доступа.</li>
 *     <li>{@link org.springframework.data.web.config.EnableSpringDataWebSupport @EnableSpringDataWebSupport}:
 *         Включает поддержку Spring Data для веб-слоя Spring MVC. Это позволяет, например,
 *         автоматически разрешать параметры {@link org.springframework.data.domain.Pageable Pageable}
 *         и {@link org.springframework.data.domain.Sort Sort} в методах контроллеров.
 *         <p>Параметр {@code pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO}
 *         указывает, что при сериализации объектов {@link org.springframework.data.domain.Page Page}
 *         (например, при возврате постраничных данных из REST API) будет использоваться
 *         подход на основе DTO (Data Transfer Object). Это часто применяется для
 *         кастомизации структуры ответа, сокрытия внутренних деталей сущностей
 *         или оптимизации производительности.</p></li>
 * </ul>
 */
@SpringBootApplication
@EnableMethodSecurity
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class BankCardsApplication {
    /**
     * <p>Основной метод, запускающий Spring Boot приложение.</p>
     *
     * <p>Этот метод является точкой входа для JVM. Он вызывает
     * {@link org.springframework.boot.SpringApplication#run(Class, String...) SpringApplication.run()},
     * который инициализирует и запускает контекст приложения Spring Boot,
     * а также выполняет все необходимые автоконфигурации и сканирование компонентов.</p>
     *
     * @param args Аргументы командной строки, переданные при запуске приложения.
     */
    public static void main(String[] args) {
        SpringApplication.run(BankCardsApplication.class, args);
    }
}
