package com.example.bankcards.entity;

import com.example.bankcards.util.CryptoConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

/**
 * JPA сущность, представляющая банковскую карту.
 * Содержит полную информацию о банковской карте, включая номер карты (зашифрованный),
 * дату истечения, баланс, текущий статус и связь с владельцем карты (пользователем).
 * <p>
 * Номер карты хранится в зашифрованном виде с использованием {@link CryptoConverter},
 * а также его хеш для обеспечения возможности поиска без расшифровки и повышения безопасности.
 * </p>
 */
@Entity
@Table(name = "bank_cards")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BankCard {
    /**
     * Уникальный идентификатор банковской карты в базе данных.
     * Генерируется автоматически при создании новой карты.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Номер банковской карты.
     * Хранится в зашифрованном виде в базе данных с помощью {@link com.example.bankcards.util.CryptoConverter}.
     * Это поле должно быть уникальным для каждой карты.
     */
    @Column(name = "card_number", nullable = false, unique = true)
    @Convert(converter = CryptoConverter.class)
    private String cardNumber;

    /**
     * Хеш номера банковской карты.
     * Используется для быстрого поиска карты (например, по последним 4 цифрам или для проверки существования)
     * без необходимости расшифровки полного номера. Это поле не может быть изменено после создания записи,
     * обеспечивая целостность.
     */
    @Column(name = "card_number_hash", nullable = false, updatable = false)
    private String cardNumberHash;

    /**
     * Дата истечения срока действия банковской карты.
     * Хранится в формате "год-месяц" (например, 2025-12).
     */
    @Column(name = "expiry_date", nullable = false)
    private YearMonth expiryDate;

    /**
     * Текущий баланс на банковской карте.
     * Использует {@link java.math.BigDecimal} для точного хранения денежных сумм.
     * Определена точность (19 знаков) и масштаб (2 знака после запятой).
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    /**
     * Текущий статус банковской карты.
     * Хранится как строковое представление значения перечисления {@link com.example.bankcards.entity.CardStatus}
     * (например, "ACTIVE", "BLOCKED", "EXPIRED").
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    /**
     * Пользователь, которому принадлежит данная банковская карта.
     * Устанавливает отношение "многие к одному" (ManyToOne) с сущностью {@link com.example.bankcards.entity.User}.
     * Загружается лениво (LAZY) для оптимизации производительности.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    /**
     * Возвращает маскированный номер банковской карты.
     * Отображает только последние 4 цифры карты, остальное заменяется звездочками.
     * Это поле не сохраняется в базе данных, так как оно помечено как {@code @Transient}.
     *
     * @return Строка с маскированным номером карты (например, "**** **** **** 1234").
     *         Возвращает "****", если исходный номер карты равен null или имеет длину менее 4 символов.
     */
    @Transient
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

/**
 * Сравнивает данный объект {@code BankCard} с другим объектом.
 * Сравнение основано на уникальном идентификаторе {@code id} карты.
 * Это стандартная реализация для JPA-сущностей, чтобы корректно
 * работать с коллекциями и persistence-контекстом, учитывая, что
 * {@code id} может быть {@code null} до сохранения сущности.
 *
 * @param o Объект для сравнения.
 * @return {@code true}, если объекты равны (имеют одинаковый ID и не {@code null}), иначе {@code false}.
 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankCard bankCard = (BankCard) o;
        return id != null && Objects.equals(id, bankCard.id);
    }

    /**
     * Вычисляет хеш-код для данного объекта {@code BankCard}.
     * Реализация основана на хеш-коде класса, что является общепринятой практикой
     * для JPA-сущностей для предотвращения проблем с производительностью
     * и корректной работой в коллекциях до того, как сущность получит ID.
     *
     * @return Хеш-код объекта.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
