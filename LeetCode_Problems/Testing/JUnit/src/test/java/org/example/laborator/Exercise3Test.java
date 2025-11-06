package org.example.laborator;

import org.example.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Exercise3Test {

    private static Exercise3 bank;

    @BeforeAll
    static void createInstance() {
        bank = new Exercise3();
    }

    @Test
    void givenAccountFromIsNull_whenTransferMoney_thenThrowIllegalArgumentException() {
        Account from = null;
        var to = new Account(300);

        assertThrows(IllegalArgumentException.class, () -> bank.transferMoney(from, to, 50));
    }

    @Test
    void givenAccountToIsNull_whenTransferMoney_thenThrowIllegalArgumentException() {
        var from = new Account(100);
        Account to = null;

        assertThrows(IllegalArgumentException.class, () -> bank.transferMoney(from, to, 50));
    }

    @Test
    void givenSentAmountIsZero_whenTransferMoney_thenThrowIllegalArgumentException() {
        var from = new Account(100);
        var to = new Account(300);

        assertThrows(IllegalArgumentException.class, () -> bank.transferMoney(from, to, 0));
    }

    @Test
    void givenInsufficientBalanceInFromAccount_whenTransferMoney_thenThrowRuntimeException() {
        var from = new Account(100);
        var to = new Account(300);

        assertThrows(RuntimeException.class, () -> bank.transferMoney(from, to, 150));
    }

    @Test
    void givenAllDataValid_whenTransferMoney_thenReturnTrue() {
        var from = new Account(100);
        var to = new Account(300);

        boolean check = bank.transferMoney(from, to, 50);

        assertTrue(check);
        assertEquals(50.0, from.getMoneyAmount());
        assertEquals(350.0, to.getMoneyAmount());
    }

    @Test
    void givenExactBalanceTransfer_whenTransferMoney_thenReturnTrueAndFromBalanceZero() {
        var from = new Account(100);
        var to = new Account(300);

        boolean check = bank.transferMoney(from, to, 100);

        assertTrue(check);
        assertEquals(0.0, from.getMoneyAmount(), 0.001);  // See note on delta below
        assertEquals(400.0, to.getMoneyAmount(), 0.001);
    }

    @Test
    void givenFractionalAmount_whenTransferMoney_thenReturnTrueAndBalancesUpdated() {
        var from = new Account(100.75);
        var to = new Account(300.25);

        boolean check = bank.transferMoney(from, to, 0.50);

        assertTrue(check);
        assertEquals(100.25, from.getMoneyAmount(), 0.001);
        assertEquals(300.75, to.getMoneyAmount(), 0.001);
    }

}