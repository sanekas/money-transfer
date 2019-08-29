package edu.sanekas.moneytransfer.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

public class TransactionTest {
    @Test
    public void testSimpleTransaction() {
        final Account fromAcc = new Account(0);
        final Account toAcc = new Account(1);
        final long amountToTransfer = 1000;
        fromAcc.debit(amountToTransfer);
        final Transaction transaction = new Transaction(fromAcc, toAcc, amountToTransfer);
        final boolean transactionRes = transaction.execute();
        Assert.assertTrue("Transaction result should be true", transactionRes);
        Assert.assertEquals("FromAcc should be empty", 0, fromAcc.getTotalMoney());
        Assert.assertEquals("ToAcc should be have " + amountToTransfer, amountToTransfer, toAcc.getTotalMoney());
    }

    @Test
    public void testTransactionWithNotEnoughMoney() {
        final Account fromAcc = new Account(0);
        final Account toAcc = new Account(1);
        final long amountToTransfer = 1000;
        final Transaction transaction = new Transaction(fromAcc, toAcc, amountToTransfer);
        final boolean transactionRes = transaction.execute();
        Assert.assertFalse("Transaction result should be false", transactionRes);
        Assert.assertEquals("FromAcc should be empty", 0, fromAcc.getTotalMoney());
        Assert.assertEquals("ToAcc should be empty", 0, toAcc.getTotalMoney());
    }

    @Test
    public void testTransactionWithNotEnoughMoneyFromAccNotEmpty() {
        final Account fromAcc = new Account(0);
        final Account toAcc = new Account(1);
        final long amountToTransfer = 1200;
        fromAcc.debit(1000);
        final Transaction transaction = new Transaction(fromAcc, toAcc, amountToTransfer);
        final boolean transactionRes = transaction.execute();
        Assert.assertFalse("Transaction result should be false", transactionRes);
        Assert.assertEquals("FromAcc should have 1000", 1000, fromAcc.getTotalMoney());
        Assert.assertEquals("ToAcc should be empty", 0, toAcc.getTotalMoney());
    }

    @Test
    public void testTransactionsWithNonEmptyAccounts() {
        final Account fromAcc = new Account(0);
        final Account toAcc = new Account(1);
        final long amountToTransfer = 900;
        fromAcc.debit(1000);
        toAcc.debit(amountToTransfer);
        final Transaction transaction = new Transaction(fromAcc, toAcc, amountToTransfer);
        final boolean transactionRes = transaction.execute();
        Assert.assertTrue("Transaction result should be true", transactionRes);
        Assert.assertEquals("FromAcc should have 100", 100, fromAcc.getTotalMoney());
        Assert.assertEquals("ToAcc should have 1800", 1800, toAcc.getTotalMoney());
    }

    @Test
    public void testConcurrentTransaction() {
        final Account fromAcc = new Account(0);
        final Account toAcc = new Account(1);
        fromAcc.debit(200);
        toAcc.debit(450);
        final Callable<Boolean> fstTrans = () -> new Transaction(fromAcc, toAcc, 50).execute();
        final Callable<Boolean> sndTrans = () -> new Transaction(toAcc, fromAcc, 350).execute();
        Stream.of(fstTrans, sndTrans)
                .map(trans -> ForkJoinPool.commonPool().submit(trans))
                .forEach(f -> {
                    try {
                        f.get();
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                });

        Assert.assertEquals("Acc: 200; <- 350; -> 50", 500, fromAcc.getTotalMoney());
        Assert.assertEquals(150, toAcc.getTotalMoney());
    }

    @Test
    public void testTransactionWithEqualAccount() {
        final Account acc = new Account(0);
        final boolean transactionResult = new Transaction(acc, acc, 100).execute();
        Assert.assertFalse("Transaction between 1 account is invalid", transactionResult);
    }

    @Test
    public void testTransactionWithEqualsAccount() {
        final Account fromAcc = new Account(0);
        final Account toAcc = new Account(1);
        final boolean transactionResult = new Transaction(fromAcc, toAcc, 0).execute();
        Assert.assertFalse("Could not transsfer 0 money", transactionResult);
    }
}
