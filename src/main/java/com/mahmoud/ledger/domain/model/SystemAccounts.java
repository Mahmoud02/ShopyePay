package com.mahmoud.ledger.domain.model;

import java.util.UUID;

public final class SystemAccounts {

    private SystemAccounts() {
    } // Prevent instantiation

    public static final UUID GENESIS_ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    // Using a separate ID for Revenue (Company Wallet)
    public static final UUID REVENUE_ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
}
