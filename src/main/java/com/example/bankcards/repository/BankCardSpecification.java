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

@Component
public class BankCardSpecification {
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
