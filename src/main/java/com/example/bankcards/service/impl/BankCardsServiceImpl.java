package com.example.bankcards.service.impl;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CreateBankCardDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.BankCardSpecification;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.BankCardsService;
import com.example.bankcards.util.BankCardMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.util.Base64;

/**
 * Сервис {@code BankCardsServiceImpl} предоставляет функциональность для управления банковскими картами.
 * Он является реализацией интерфейса {@link com.example.bankcards.service.BankCardsService}
 * и отвечает за создание, получение, изменение статуса, удаление карт, а также обработку запросов на блокировку.
 * <p>
 * Класс использует {@link BankCardsRepository} для взаимодействия с базой данных,
 * {@link UserRepository} для работы с данными пользователей и {@link BankCardMapper} для преобразования DTO в сущности и обратно.
 * Для повышения безопасности и возможности поиска по части номера карты, сервис использует хэширование номеров карт.
 * </p>
 */
@Service
public class BankCardsServiceImpl implements BankCardsService {
    /**
     * Спецификация для построения динамических запросов к банковским картам.
     */
    private final BankCardSpecification bankCardSpecification;
    /**
     * Репозиторий для доступа к данным банковских карт.
     */
    private final BankCardsRepository bankCardsRepository;
    /**
     * Репозиторий для доступа к данным пользователей, владельцев карт.
     */
    private final UserRepository userRepository;
    /**
     * Маппер для преобразования между DTO и сущностями банковских карт.
     */
    private final BankCardMapper bankCardMapper;
    /**
     * Соль (pepper) для хэширования номеров карт, используемая для создания индекса
     * и предотвращения прямых коллизий или атак по словарю.
     * Значение внедряется из конфигурации приложения.
     */
    @Value("${encryption.hash-pepper}")
    private String hashPepper;

    /**
     * Конструктор для внедрения необходимых зависимостей.
     *
     * @param bankCardSpecification Спецификация для построения динамических запросов к банковским картам.
     * @param bankCardsRepository   Репозиторий для доступа к данным банковских карт.
     * @param userRepository        Репозиторий для доступа к данным пользователей.
     * @param bankCardMapper        Маппер для преобразования между DTO и сущностями банковских карт.
     */
    public BankCardsServiceImpl(BankCardSpecification bankCardSpecification, BankCardsRepository bankCardsRepository,
                                UserRepository userRepository, BankCardMapper bankCardMapper) {
        this.bankCardSpecification = bankCardSpecification;
        this.bankCardsRepository = bankCardsRepository;
        this.userRepository = userRepository;
        this.bankCardMapper = bankCardMapper;
    }

    /**
     * Создает новую банковскую карту в системе.
     * <p>
     * Метод выполняет следующие действия:
     * <ol>
     *     <li>Находит пользователя-владельца карты по его ID. Если пользователь не найден, выбрасывается {@link NotFoundException}.</li>
     *     <li>Преобразует {@link CreateBankCardDto} в сущность {@link BankCard}.</li>
     *     <li>Устанавливает владельца, начальный баланс ({@link BigDecimal#ZERO}) и статус карты ({@link CardStatus#ACTIVE}).</li>
     *     <li>Генерирует хэш номера карты для индексации и сохранения в базе данных, используя {@link #createHashedIndex(String)}.</li>
     *     <li>Сохраняет новую карту в репозитории. Если карта с таким номером уже существует (нарушение уникальности),
     *         выбрасывается {@link DataIntegrityViolationException}.</li>
     * </ol>
     * </p>
     *
     * @param createDto Объект {@link CreateBankCardDto}, содержащий данные для создания новой карты.
     * @throws NotFoundException               Если пользователь с указанным {@code ownerId} не найден.
     * @throws DataIntegrityViolationException Если карта с таким номером уже существует в системе.
     */
    @Override
    @Transactional
    public void create(CreateBankCardDto createDto) {
        User owner = userRepository.findById(createDto.getOwnerId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + createDto.getOwnerId() + " не найден"));

        BankCard newCard = bankCardMapper.toEntity(createDto);
        newCard.setOwner(owner);
        newCard.setBalance(BigDecimal.ZERO);
        newCard.setStatus(CardStatus.ACTIVE);

        String plainCardNumber = createDto.getCardNumber();

        newCard.setCardNumberHash(createHashedIndex(plainCardNumber));

        try {
            bankCardsRepository.save(newCard);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Карта с таким номером уже существует.");
        }
    }

    /**
     * Возвращает страницу всех банковских карт, доступных в системе.
     * <p>
     * Метод выполняет запрос ко всем картам с учетом пагинации и преобразует
     * сущности {@link BankCard} в {@link BankCardDto}.
     * </p>
     *
     * @param pageable Объект {@link Pageable}, содержащий информацию о пагинации (номер страницы, размер страницы, сортировка).
     * @return Объект {@link Page} с {@link BankCardDto}, представляющий страницу банковских карт.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BankCardDto> getAllCards(Pageable pageable) {
        return bankCardsRepository.findAll(pageable)
                .map(bankCardMapper::toDto);
    }

    /**
     * Блокирует банковскую карту по ее идентификатору.
     * <p>
     * Метод вызывает вспомогательный метод {@link #changeCardStatus(Long, CardStatus)}
     * для изменения статуса карты на {@link CardStatus#BLOCKED}.
     * </p>
     *
     * @param cardId Идентификатор (ID) банковской карты, которую необходимо заблокировать.
     */
    @Override
    @Transactional
    public void blockCard(Long cardId) {
        changeCardStatus(cardId, CardStatus.BLOCKED);
    }

    /**
     * Активирует банковскую карту по ее идентификатору.
     * <p>
     * Метод вызывает вспомогательный метод {@link #changeCardStatus(Long, CardStatus)}
     * для изменения статуса карты на {@link CardStatus#ACTIVE}.
     * </p>
     *
     * @param cardId Идентификатор (ID) банковской карты, которую необходимо активировать.
     */
    @Override
    @Transactional
    public void activateCard(Long cardId) {
        changeCardStatus(cardId, CardStatus.ACTIVE);
    }

    /**
     * Удаляет банковскую карту из системы по ее идентификатору.
     *
     * @param cardId Идентификатор (ID) банковской карты, которую необходимо удалить.
     */
    @Override
    @Transactional
    public void delete(Long cardId) {
        bankCardsRepository.deleteById(cardId);
    }

    /**
     * Обрабатывает запрос пользователя на блокировку своей банковской карты.
     * <p>
     * Метод выполняет следующие действия:
     * <ol>
     *     <li>Получает текущего аутентифицированного пользователя из контекста безопасности.</li>
     *     <li>Находит банковскую карту по ее ID. Если карта не найдена, выбрасывается {@link NotFoundException}.</li>
     *     <li>Проверяет, является ли текущий аутентифицированный пользователь владельцем данной карты.
     *         Если нет, выбрасывается {@link BadCredentialsException} для предотвращения несанкционированного доступа.</li>
     *     <li>Изменяет статус карты на {@link CardStatus#BLOCKED}.</li>
     * </ol>
     * </p>
     *
     * @param cardId Идентификатор (ID) банковской карты, которую пользователь хочет заблокировать.
     * @throws BadCredentialsException Если текущий аутентифицированный пользователь не является владельцем карты.
     * @throws NotFoundException       Если карта с указанным ID не найдена.
     */
    @Override
    @Transactional
    public void requestBlock(Long cardId) {
        User currentUser = getAuthenticatedUser();
        BankCard card = findCardByIdOrThrow(cardId);

        if (!card.getOwner().getId().equals(currentUser.getId())) {
            throw new BadCredentialsException("Доступ запрещен: вы не являетесь владельцем данной карты.");
        }

        card.setStatus(CardStatus.BLOCKED);
        bankCardsRepository.save(card);
    }

    /**
     * Ищет и возвращает список банковских карт текущего аутентифицированного пользователя
     * с возможностью фильтрации по статусу, минимальному балансу и сроку действия, а также пагинации.
     * <p>
     * Метод строит динамическую спецификацию запроса на основе переданных параметров
     * и применяет её для получения страниц карт из репозитория.
     * </p>
     *
     * @param status         Необязательный параметр для фильтрации по статусу карты (например, {@link CardStatus#ACTIVE}, {@link CardStatus#BLOCKED}).
     * @param minBalance     Необязательный параметр для фильтрации по минимальному балансу.
     * @param expiryDateFrom Необязательный параметр для фильтрации карт со сроком действия, начиная с указанной даты.
     * @param pageable       Объект {@link Pageable} для пагинации и сортировки результатов.
     * @return {@link Page} объектов {@link BankCardDto}, соответствующих критериям поиска.
     * @throws NotFoundException Если аутентифицированный пользователь не найден в системе.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BankCardDto> searchCurrentUserCards(CardStatus status, BigDecimal minBalance,
                                                    YearMonth expiryDateFrom, Pageable pageable) {
        User currentUser = getAuthenticatedUser();

        Specification<BankCard> spec = bankCardSpecification
                .build(currentUser.getId(), status, minBalance, expiryDateFrom);

        Page<BankCard> cardsPage = bankCardsRepository.findAll(spec, pageable);

        return cardsPage.map(bankCardMapper::toDto);
    }

    /**
     * Изменяет статус банковской карты.
     * <p>
     * Этот приватный метод является вспомогательным для {@link #blockCard(Long)} и {@link #activateCard(Long)}.
     * Он находит карту по ID и обновляет ее статус, затем сохраняет изменения.
     * </p>
     *
     * @param cardId    Идентификатор (ID) банковской карты.
     * @param newStatus Новый статус для карты (например, {@link CardStatus#ACTIVE} или {@link CardStatus#BLOCKED}).
     * @throws NotFoundException Если карта с указанным ID не найдена.
     */
    private void changeCardStatus(Long cardId, CardStatus newStatus) {
        BankCard card = findCardByIdOrThrow(cardId);

        if (card.getStatus() != newStatus) {
            card.setStatus(newStatus);
            bankCardsRepository.save(card);
        }
    }

    /**
     * Находит банковскую карту по ее идентификатору или выбрасывает исключение {@link NotFoundException}.
     * <p>
     * Этот приватный вспомогательный метод используется для сокращения повторяющегося кода
     * при поиске карты, когда ее отсутствие должно приводить к ошибке.
     * </p>
     *
     * @param cardId Идентификатор (ID) банковской карты для поиска.
     * @return Найденная сущность {@link BankCard}.
     * @throws NotFoundException Если карта с указанным ID не найдена.
     */
    private BankCard findCardByIdOrThrow(Long cardId) {
        return bankCardsRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта с ID " + cardId + " не найдена"));
    }

    /**
     * Извлекает текущего аутентифицированного пользователя из контекста безопасности Spring Security.
     * <p>
     * Этот приватный метод используется для получения информации о пользователе,
     * который выполняет текущий запрос.
     * </p>
     *
     * @return Объект {@link User} текущего аутентифицированного пользователя.
     * @throws BadCredentialsException Если пользователь не аутентифицирован или не найден
     *                                 (например, если {@code SecurityContextHolder} не содержит аутентификации
     *                                 или принцип не является экземпляром {@link User}).
     */
    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Аутентифицированный пользователь " + username
                        + " не найден в системе"));
    }

    /**
     * Генерирует хэшированный индекс для номера банковской карты.
     * <p>
     * Используется алгоритм SHA-256 в комбинации с "солью" ({@code hashPepper})
     * для создания уникального и безопасного хэша номера карты.
     * Этот хэш может быть использован для индексации и поиска карт без хранения
     * исходного номера карты в открытом виде.
     * </p>
     *
     * @param plainText Исходный номер банковской карты без хэширования.
     * @return Строка, представляющая хэшированный индекс номера карты в формате Base64.
     * @throws IllegalStateException Если алгоритм хэширования SHA-256 недоступен в среде выполнения Java.
     */
    private String createHashedIndex(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String textWithPepper = plainText + hashPepper;

            byte[] hash = digest.digest(textWithPepper.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new NotFoundException("Не удалось найти алгоритм хэширования");
        }
    }
}
