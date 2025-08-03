package com.example.bankcards.controller;

import com.example.bankcards.service.BankCardsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bankcards")
@Tag(name = "Управление банковскими картами", description = "API для создания, получения и удаления банковских карт.")
@SecurityRequirement(name = "bearerAuth")
public class BankCardController {
    private final BankCardsService bankCardsService;

    public BankCardController(BankCardsService bankCardsService) {
        this.bankCardsService = bankCardsService;
    }
}
