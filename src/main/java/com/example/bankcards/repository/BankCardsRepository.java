package com.example.bankcards.repository;

import com.example.bankcards.entity.BankCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;


public interface BankCardsRepository extends JpaRepository<BankCard, Long>, JpaSpecificationExecutor<BankCard> {
    @Query("SELECT SUM(b.balance) FROM BankCard b WHERE b.owner.id = :userId")
    BigDecimal calculateTotalBalanceByUserId(@Param("userId") Long userId);
}
