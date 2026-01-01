package com.mahmoud.ledger.application.port.in;

import java.math.BigDecimal;

public record CreateAccountCommand(String name, String currency) {
    public CreateAccountCommand {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be empty");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("Currency cannot be empty");
    }
}
