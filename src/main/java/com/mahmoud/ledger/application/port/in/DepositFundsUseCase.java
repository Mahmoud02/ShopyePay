package com.mahmoud.ledger.application.port.in;

import java.util.UUID;

public interface DepositFundsUseCase {
    UUID depositFunds(DepositFundsCommand command);
}
