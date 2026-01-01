package com.mahmoud.ledger;

import com.mahmoud.ledger.application.port.in.CreateAccountCommand;
import com.mahmoud.ledger.application.port.in.PostTransactionCommand;
import com.mahmoud.ledger.application.port.in.PostingCommand;
import com.mahmoud.ledger.application.port.in.TransferFundsCommand;
import com.mahmoud.ledger.domain.model.AccountType;
import com.mahmoud.ledger.domain.model.Posting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("test")
public class LedgerIntegrationTest {

        @Autowired
        private RestTestClient restClient;

        @Test
        void given_NewAccount_When_DepositFunds_Then_BalanceIsUpdated() {
                // 1. Create a "Genesis" account (Equity)
                CreateAccountCommand genesisCmd = new CreateAccountCommand("Genesis", AccountType.EQUITY, "USD");
                UUID genesisId = createAccountHelper(genesisCmd);

                // 2. Create the User's account (Asset)
                CreateAccountCommand userCmd = new CreateAccountCommand("Alice", AccountType.ASSET, "USD");
                UUID aliceId = createAccountHelper(userCmd);

                // 3. Deposit (Genesis -> Alice)
                PostTransactionCommand depositTransaction = new PostTransactionCommand(
                                "Deposit to Alice",
                                List.of(
                                                new PostingCommand(genesisId, new BigDecimal("100.00"), "USD",
                                                                Posting.Type.CREDIT),
                                                new PostingCommand(aliceId, new BigDecimal("100.00"), "USD",
                                                                Posting.Type.DEBIT)));

                restClient.post().uri("/api/transactions")
                                .body(depositTransaction)
                                .exchange()
                                .expectStatus().isOk();

                // 4. Verify Balance
                verifyBalance(aliceId, new BigDecimal("100.0"));
        }

        @Test
        void given_FundedAccounts_When_TransferFunds_Then_BalancesAreUpdated() {
                // 1. Create Accounts
                UUID genesisId = createAccountHelper(
                                new CreateAccountCommand("Genesis", AccountType.EQUITY, "USD"));
                UUID aliceId = createAccountHelper(new CreateAccountCommand("Alice", AccountType.ASSET, "USD"));
                UUID bobId = createAccountHelper(new CreateAccountCommand("Bob", AccountType.ASSET, "USD"));

                // 2. Fund Alice (Genesis -> Alice)
                PostTransactionCommand fundAlice = new PostTransactionCommand(
                                "Fund Alice",
                                List.of(
                                                new PostingCommand(genesisId, new BigDecimal("100.00"), "USD",
                                                                Posting.Type.CREDIT),
                                                new PostingCommand(aliceId, new BigDecimal("100.00"), "USD",
                                                                Posting.Type.DEBIT)));

                restClient.post().uri("/api/transactions")
                                .body(fundAlice)
                                .exchange()
                                .expectStatus().isOk();

                // 3. Transfer Alice -> Bob
                TransferFundsCommand transferCmd = new TransferFundsCommand(aliceId, bobId, new BigDecimal("40.00"),
                                "USD", "E2E Transfer");

                restClient.post().uri("/api/transfers")
                                .body(transferCmd)
                                .exchange()
                                .expectStatus().isOk();

                // 4. Verify Final Balances
                // Alice: 100 - 40 = 60
                verifyBalance(aliceId, new BigDecimal("60.0"));
                // Bob: 0 + 40 = 40
                verifyBalance(bobId, new BigDecimal("40.0"));
        }

        @Test
        void given_TransferWithFee_When_Executed_Then_RevenueAccountIsCredited() {
                // 1. Create Accounts
                // "Revenue" account here acts as the Company Wallet (Asset)
                UUID revenueId = createAccountHelper(
                                new CreateAccountCommand("Company Wallet", AccountType.ASSET, "USD"));
                UUID genesisId = createAccountHelper(new CreateAccountCommand("Genesis", AccountType.EQUITY, "USD"));
                UUID aliceId = createAccountHelper(new CreateAccountCommand("Alice", AccountType.ASSET, "USD"));
                UUID bobId = createAccountHelper(new CreateAccountCommand("Bob", AccountType.ASSET, "USD"));

                // 2. Fund Alice with 100 USD (Genesis -> Alice)
                PostTransactionCommand fundAlice = new PostTransactionCommand(
                                "Fund Alice",
                                List.of(
                                                new PostingCommand(genesisId, new BigDecimal("100.00"), "USD",
                                                                Posting.Type.CREDIT),
                                                new PostingCommand(aliceId, new BigDecimal("100.00"), "USD",
                                                                Posting.Type.DEBIT)));
                restClient.post().uri("/api/transactions")
                                .body(fundAlice)
                                .exchange()
                                .expectStatus().isOk();

                // 3. Transfer 50 USD from Alice to Bob with Revenue ID
                // Fee = 10% of 50 = 5.00
                // Bob gets 45.00
                // Revenue gets 5.00
                TransferFundsCommand transferCmd = new TransferFundsCommand(
                                aliceId,
                                bobId,
                                new BigDecimal("50.00"),
                                "USD",
                                "Transfer with Fee",
                                revenueId // Pass revenueAccountId
                );

                restClient.post().uri("/api/transfers")
                                .body(transferCmd)
                                .exchange()
                                .expectStatus().isOk();

                // 4. Verify Balances
                // Alice: 100 - 50 = 50
                verifyBalance(aliceId, new BigDecimal("50.0"));

                // Bob: 0 + 45 = 45
                verifyBalance(bobId, new BigDecimal("45.0"));

                // Revenue (Company Wallet):
                // Initial: 0
                // Fee Received: +5 (Debit)
                // Final: 5.0
                verifyBalance(revenueId, new BigDecimal("5.0"));
        }

        private UUID createAccountHelper(CreateAccountCommand cmd) {
                return restClient.post().uri("/api/accounts")
                                .body(cmd)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(UUID.class)
                                .returnResult().getResponseBody();
        }

        private void verifyBalance(UUID accountId, BigDecimal expectedAmount) {
                restClient.get().uri("/api/accounts/" + accountId)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.balance").isEqualTo(expectedAmount.doubleValue());
        }
}
