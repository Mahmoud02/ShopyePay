package com.mahmoud.ledger.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferFundsCommand(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String currency,
        String description, UUID revenueAccountId) {
    public TransferFundsCommand(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String currency,
            String description) {
        this(fromAccountId, toAccountId, amount, currency, description, null);
    }

    public TransferFundsCommand {
        if (fromAccountId == null)
            throw new IllegalArgumentException("Source account ID cannot be null");
        if (toAccountId == null)
            throw new IllegalArgumentException("Destination account ID cannot be null");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("Currency cannot be empty");
    }
}
