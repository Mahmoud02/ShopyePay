package com.mahmoud.ledger.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void testPostDebitIncreasesAssetBalance() {
        // Asset Account: Debit = Increase
        Account account = Account.create(UUID.randomUUID(), "Test Main", "USD");

        Posting debit = new Posting(account.getId(), Money.of(new BigDecimal("100"), "USD"), Posting.Type.DEBIT);
        account.postPosting(debit);

        assertEquals(new BigDecimal("100"), account.getBalance().amount());
    }

    @Test
    void testPostCreditDecreasesAssetBalance() {
        // Asset Account: Credit = Decrease
        Account account = Account.create(UUID.randomUUID(), "Test Main", "USD");

        // First add some money
        account.postPosting(new Posting(account.getId(), Money.of(new BigDecimal("100"), "USD"), Posting.Type.DEBIT));

        // Now spend it (Credit)
        Posting credit = new Posting(account.getId(), Money.of(new BigDecimal("40"), "USD"), Posting.Type.CREDIT);
        account.postPosting(credit);

        assertEquals(new BigDecimal("60"), account.getBalance().amount());
    }

    @Test
    void testCurrencyMismatchThrowsException() {
        Account account = Account.create(UUID.randomUUID(), "Test USD", "USD");
        Posting euroPosting = new Posting(account.getId(), Money.of(new BigDecimal("100"), "EUR"), Posting.Type.DEBIT);

        assertThrows(IllegalArgumentException.class, () -> account.postPosting(euroPosting));
    }
}
