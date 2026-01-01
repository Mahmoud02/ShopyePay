package com.mahmoud.ledger.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void testCreation() {
        Money money = Money.of(new BigDecimal("100.00"), "USD");
        assertNotNull(money);
        assertEquals(new BigDecimal("100.00"), money.amount());
        assertEquals("USD", money.currency().getCurrencyCode());
    }

    @Test
    void testAddSameCurrency() {
        Money m1 = Money.of(new BigDecimal("50"), "USD");
        Money m2 = Money.of(new BigDecimal("25"), "USD");
        Money result = m1.add(new Money(m2.amount(), m2.currency()));

        assertEquals(new BigDecimal("75"), result.amount());
    }

    @Test
    void testSubtractSameCurrency() {
        Money m1 = Money.of(new BigDecimal("50"), "USD");
        Money m2 = Money.of(new BigDecimal("25"), "USD");
        Money result = m1.subtract(new Money(m2.amount(), m2.currency()));

        assertEquals(new BigDecimal("25"), result.amount());
    }

    @Test
    void testCurrencyMismatchThrowsException() {
        Money m1 = Money.of(new BigDecimal("50"), "USD");
        Money m2 = Money.of(new BigDecimal("50"), "EUR");

        assertThrows(IllegalArgumentException.class, () -> m1.add(new Money(m2.amount(), m2.currency())));
    }
}
