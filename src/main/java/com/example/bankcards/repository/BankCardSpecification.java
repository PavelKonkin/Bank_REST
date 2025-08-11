package com.example.bankcards.repository;

import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.BankCard_;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User_;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Компонент Spring, предназначенный для построения динамических запросов (спецификаций)
 * для сущности {@link BankCard} с использованием JPA Criteria API.
 * <p>
 * Этот класс позволяет создавать {@link Specification}, которые можно использовать
 * со Spring Data JPA репозиториями, реализующими {@link org.springframework.data.jpa.repository.JpaSpecificationExecutor},
 * для фильтрации банковских карт по различным критериям.
 */
@Component
public class BankCardSpecification {
    /**
     * Строит {@link Specification} для сущности {@link BankCard} на основе предоставленных критериев фильтрации.
     * <p>
     * Все не-{@code null} параметры будут применены как условия {@code AND} к запросу.
     * Фильтрация по идентификатору владельца карты ({@code ownerId}) является обязательной и всегда применяется.
     *
     * @param ownerId Идентификатор владельца карты. Этот параметр является обязательным.
     * @param status Статус карты (например, {@link CardStatus#ACTIVE}, {@link CardStatus#BLOCKED}).
     *               Если {@code null}, этот критерий не применяется.
     * @param minBalance Минимальный баланс карты. Карты с балансом меньше указанного будут исключены.
     *                   Если {@code null}, этот критерий не применяется.
     * @param expiryDateFrom Минимальная дата истечения срока действия карты. Карты, срок действия которых
     *                       истекает раньше указанной даты, будут исключены.
     *                       Если {@code null}, этот критерий не применяется.
     * @return {@link Specification}, который можно использовать для выполнения запросов к {@link BankCard}
     *         с примененными фильтрами.
     */
    public Specification<BankCard> build(Long ownerId, CardStatus status,
                                         BigDecimal minBalance, YearMonth expiryDateFrom) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get(BankCard_.owner).get(User_.id), ownerId));

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get(BankCard_.status), status));
            }

            if (minBalance != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(BankCard_.balance), minBalance));
            }

            if (expiryDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(BankCard_.expiryDate),
                        expiryDateFrom));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
