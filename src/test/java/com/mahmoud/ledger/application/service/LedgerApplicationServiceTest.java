package com.mahmoud.ledger.application.service;

import com.mahmoud.ledger.application.port.in.PostTransactionCommand;
import com.mahmoud.ledger.application.port.in.PostingCommand;
import com.mahmoud.ledger.application.port.in.TransferFundsCommand;
import com.mahmoud.ledger.application.port.out.AccountPort;
import com.mahmoud.ledger.application.port.out.TransactionPort;
import com.mahmoud.ledger.domain.model.Account;
import com.mahmoud.ledger.domain.model.Money;
import com.mahmoud.ledger.domain.model.Posting;
import com.mahmoud.ledger.domain.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerApplicationServiceTest {

    @Mock
    private AccountPort accountPort;

    @Mock
    private TransactionPort transactionPort;

    private LedgerApplicationService service;

    @BeforeEach
    void setUp() {
        service = new LedgerApplicationService(accountPort, transactionPort);
    }

    @Test
    void testPostTransactionSuccess() {
        UUID acc1 = UUID.randomUUID();
        UUID acc2 = UUID.randomUUID();

        // 1. Prepare Mocks
        Account account1 = Account.create(acc1, "Acc 1", "USD");
        Account account2 = Account.create(acc2, "Acc 2", "USD");

        when(accountPort.loadLocked(acc1)).thenReturn(Optional.of(account1));
        when(accountPort.loadLocked(acc2)).thenReturn(Optional.of(account2));

        // 2. Execute
        PostingCommand p1 = new PostingCommand(acc1, new BigDecimal("100"), "USD", Posting.Type.DEBIT);
        PostingCommand p2 = new PostingCommand(acc2, new BigDecimal("100"), "USD", Posting.Type.CREDIT);

        PostTransactionCommand command = new PostTransactionCommand("Test Tx", List.of(p1, p2));

        UUID txId = service.postTransaction(command);

        // 3. Verify
        assertNotNull(txId);

        // Verify Locking was used
        verify(accountPort).loadLocked(acc1);
        verify(accountPort).loadLocked(acc2);

        // Verify Balances Updated (Asset Logic)
        // Acc1 Debit 100 -> +100
        // Acc2 Credit 100 -> -100 (since it started at 0)
        assertEquals(new BigDecimal("100"), account1.getBalance().amount());
        assertEquals(new BigDecimal("-100"), account2.getBalance().amount()); // Credit means decrease

        // Verify Save called
        verify(accountPort, times(2)).save(any(Account.class));
        verify(transactionPort).save(any(Transaction.class));
    }

    @Test
    void testTransferFundsFlow() {
        UUID sourceId = UUID.randomUUID();
        UUID destId = UUID.randomUUID();

        // 1. Prepare Mocks
        Account source = Account.create(sourceId, "Source", "USD");
        Account dest = Account.create(destId, "Dest", "USD");

        // Give source some money initially (manually, to simulate existing balance)
        // We can't easily inject state into the domain object via public API except via
        // Posting.
        // But for this test, we check DELTA.

        when(accountPort.loadLocked(sourceId)).thenReturn(Optional.of(source));
        when(accountPort.loadLocked(destId)).thenReturn(Optional.of(dest));

        // 2. Execute Transfer (50 USD)
        // Logic: Credit Source (Decrease), Debit Dest (Increase)
        TransferFundsCommand command = new TransferFundsCommand(sourceId, destId, new BigDecimal("50"), "USD",
                "Transfer Test");
        service.transferFunds(command);

        // 3. Verify
        // Source (started 0) -> Credit 50 -> -50
        assertEquals(new BigDecimal("-50"), source.getBalance().amount());

        // Dest (started 0) -> Debit 50 -> +50
        assertEquals(new BigDecimal("50"), dest.getBalance().amount());

        verify(accountPort).loadLocked(sourceId);
        verify(accountPort).loadLocked(destId);
        verify(transactionPort).save(any(Transaction.class));
    }

    @Test
    void testTransactionFailsIfAccountNotFound() {
        UUID acc1 = UUID.randomUUID();
        when(accountPort.loadLocked(acc1)).thenReturn(Optional.empty());

        PostingCommand p1 = new PostingCommand(acc1, new BigDecimal("100"), "USD", Posting.Type.DEBIT);
        PostTransactionCommand command = new PostTransactionCommand("Bad Acc", List.of(p1));

        // Should actually fail validation first (unbalanced), but let's assume valid tx
        // structure
        // passing validation but failing on account lookup.
        // We need 2 postings to pass validation.
        UUID acc2 = UUID.randomUUID();
        PostingCommand p2 = new PostingCommand(acc2, new BigDecimal("100"), "USD", Posting.Type.CREDIT);

        PostTransactionCommand balancedCommand = new PostTransactionCommand("Bad Acc", List.of(p1, p2));

        assertThrows(IllegalArgumentException.class, () -> service.postTransaction(balancedCommand));
    }
}
