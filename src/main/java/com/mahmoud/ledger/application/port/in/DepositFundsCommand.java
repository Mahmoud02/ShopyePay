package com.mahmoud.ledger.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositFundsCommand(UUID accountId, BigDecimal amount, String currency, String description) {
    public DepositFundsCommand {
        if (accountId == null)
            throw new IllegalArgumentException("Account ID cannot be null");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("Currency cannot be empty");
    }
}
