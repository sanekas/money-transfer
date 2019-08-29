package edu.sanekas.moneytransfer.model;

import org.junit.Assert;
import org.junit.Test;

public class AccountTest {
    @Test
    public void testCreateAccount() {
        final Account acc = new Account(0);
        Assert.assertEquals("New account should not have money", 0, acc.getTotalMoney());
    }

    @Test
    public void testDebit() {
        final Account account = new Account(0);
        final long amount = 1000;
        final boolean debitRes = account.debit(amount);
        Assert.assertTrue("Debit should be successful", debitRes);
        Assert.assertEquals("Debit just: " + amount, amount, account.getTotalMoney());
    }

    @Test
    public void testDebitNegative() {
        final Account acc = new Account(0);
        final boolean debitRes = acc.debit(-1000);
        Assert.assertFalse("Debit should be unsuccessful for negative amount", debitRes);
    }

    @Test
    public void testWithdraw() {
        final Account acc = new Account(0);
        acc.debit(1000);
        final boolean withdrawRes = acc.withdraw(100);
        Assert.assertTrue("Withdraw should be successful", withdrawRes);
        Assert.assertEquals("Account should have 900", 900, acc.getTotalMoney());
    }

    @Test
    public void testWithdrawNegative() {
        final Account acc = new Account(0);
        acc.debit(1000);
        final boolean withdrawRes = acc.withdraw(-100);
        Assert.assertFalse("Withdraw should be unsuccessful", withdrawRes);
        Assert.assertEquals("Account should have 900", 1000, acc.getTotalMoney());
    }

    @Test
    public void testWithdrawFromEmptyAccount() {
        final Account acc = new Account(0);
        final boolean withdrawRes = acc.withdraw(100);
        Assert.assertFalse("Withdraw should be unsuccessful", withdrawRes);
    }

    @Test
    public void testWithdrawMoreThanAccountHas() {
        final Account acc = new Account(0);
        final long amount = 1000;
        acc.debit(amount);
        final boolean withdrawRes = acc.withdraw(1200);
        Assert.assertFalse("Account doesn't have enough money", withdrawRes);
        Assert.assertEquals("Account should have the same amount of money as before withdrawing",
                amount, acc.getTotalMoney());
    }
}
