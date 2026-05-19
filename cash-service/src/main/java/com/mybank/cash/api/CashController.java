package com.mybank.cash.api;

import com.mybank.cash.service.CashService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cash")
public class CashController {

    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PostMapping("/deposit")
    public CashOperationResponse deposit(@Valid @RequestBody CashOperationRequest request) {
        return cashService.deposit(request.amount());
    }

    @PostMapping("/withdraw")
    public CashOperationResponse withdraw(@Valid @RequestBody CashOperationRequest request) {
        return cashService.withdraw(request.amount());
    }
}
