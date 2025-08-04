package com.example.bankcards.repository;

import com.example.bankcards.entity.BankCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface BankCardsRepository extends JpaRepository<BankCard, Long>, JpaSpecificationExecutor<BankCard> {
}
