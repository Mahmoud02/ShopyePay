package com.mahmoud.ledger.application.port.in;

import java.util.UUID;

public interface PostTransactionUseCase {
    UUID postTransaction(PostTransactionCommand command);
}
