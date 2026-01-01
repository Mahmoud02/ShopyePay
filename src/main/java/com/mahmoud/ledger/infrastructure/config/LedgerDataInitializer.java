package com.mahmoud.ledger.infrastructure.config;

import com.mahmoud.ledger.application.port.out.AccountPort;
import com.mahmoud.ledger.domain.model.Account;
import com.mahmoud.ledger.domain.model.AccountType;
import com.mahmoud.ledger.domain.model.SystemAccounts;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LedgerDataInitializer implements CommandLineRunner {

    private final AccountPort accountPort;

    @Override
    @Transactional
    public void run(String... args) {
        initializeAccount(SystemAccounts.GENESIS_ACCOUNT_ID, "Genesis", AccountType.EQUITY);
        initializeAccount(SystemAccounts.REVENUE_ACCOUNT_ID, "Company Revenue", AccountType.ASSET);
    }

    private void initializeAccount(java.util.UUID id, String name, AccountType type) {
        if (accountPort.load(id).isEmpty()) {
            Account account = Account.create(id, name, type, "USD");
            accountPort.save(account);
            System.out.println("Initialized system account: " + name + " (" + id + ")");
        }
    }
}
