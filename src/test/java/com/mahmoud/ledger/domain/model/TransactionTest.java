package com.mahmoud.ledger.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void testValidBalancedTransaction() {
        Transaction tx = Transaction.create("Test Tx");
        UUID acc1 = UUID.randomUUID();
        UUID acc2 = UUID.randomUUID();

        tx.addPosting(new Posting(acc1, Money.of(new BigDecimal("100"), "USD"), Posting.Type.DEBIT));
        tx.addPosting(new Posting(acc2, Money.of(new BigDecimal("100"), "USD"), Posting.Type.CREDIT));

        assertDoesNotThrow(tx::validate);
    }

    @Test
    void testUnbalancedTransactionThrowsException() {
        Transaction tx = Transaction.create("Unbalanced");
        UUID acc1 = UUID.randomUUID();
        UUID acc2 = UUID.randomUUID();

        tx.addPosting(new Posting(acc1, Money.of(new BigDecimal("100"), "USD"), Posting.Type.DEBIT));
        tx.addPosting(new Posting(acc2, Money.of(new BigDecimal("50"), "USD"), Posting.Type.CREDIT));

        IllegalStateException ex = assertThrows(IllegalStateException.class, tx::validate);
        assertTrue(ex.getMessage().contains("must sum to zero"));
    }

    @Test
    void testMixedCurrenciesThrowsException() {
        Transaction tx = Transaction.create("Mixed Currency");
        UUID acc1 = UUID.randomUUID();

        tx.addPosting(new Posting(acc1, Money.of(new BigDecimal("100"), "USD"), Posting.Type.DEBIT));

        // Trying to add EUR should fail immediately in addPosting
        assertThrows(IllegalArgumentException.class, () -> tx.addPosting(
                new Posting(UUID.randomUUID(), Money.of(new BigDecimal("100"), "EUR"), Posting.Type.CREDIT)));
    }

    @Test
    void testEmptyTransactionThrowsException() {
        Transaction tx = Transaction.create("Empty");
        assertThrows(IllegalStateException.class, tx::validate);
    }
}
