package com.example.bankcards.repository;

import com.example.bankcards.entity.BankCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

/**
 * Интерфейс репозитория Spring Data JPA для сущности {@link BankCard}.
 * <p>
 * Предоставляет стандартные операции CRUD (создание, чтение, обновление, удаление)
 * для банковских карт, а также поддержку выполнения динамических запросов
 * с использованием {@link org.springframework.data.jpa.domain.Specification}
 * через интерфейс {@link JpaSpecificationExecutor}.
 * <p>
 * Используется для взаимодействия с базой данных и управления сущностями {@link BankCard}.
 */
public interface BankCardsRepository extends JpaRepository<BankCard, Long>, JpaSpecificationExecutor<BankCard> {
    /**
     * Рассчитывает общую сумму балансов всех банковских карт, принадлежащих указанному пользователю.
     * <p>
     * Использует JPQL-запрос для агрегации балансов.
     * </p>
     * @param userId Идентификатор пользователя, для которого необходимо рассчитать общий баланс.
     * @return {@link BigDecimal} – общая сумма балансов всех карт пользователя.
     *         Возвращает {@code null}, если у пользователя нет карт, или {@code 0}, если все карты имеют нулевой баланс.
     */
    @Query("SELECT SUM(b.balance) FROM BankCard b WHERE b.owner.id = :userId")
    BigDecimal calculateTotalBalanceByUserId(@Param("userId") Long userId);
}
