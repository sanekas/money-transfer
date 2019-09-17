package edu.sanekas.moneytransfer.storages;

import edu.sanekas.moneytransfer.model.Account;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppendableInMemoryAccountsStorageTest {
    private static final AccountsStorage storage = new AppendableInMemoryAccountsStorage();

    @Test
    public void testNonExistedAccount() {
        final Optional<Account> acc = storage.getAccountById(100500);
        Assert.assertTrue("Non-existed account is found", acc.isEmpty());
    }

    @Test
    public void testGetAccountById() {
        final Optional<Account> foundAcc = storage.getAccountById(0);
        Assert.assertTrue("Account is not found", foundAcc.isPresent());
    }

    @Test
    public void testCreateAccount() {
        final Account account = storage.createAccount();
        Assert.assertNotNull("Created object is null", account);
    }

    @Test
    public void testConcurrentAccountsCreation() throws InterruptedException {
        final ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final Runnable createAccountTask = storage::createAccount;
        final int accountsForCreation = 16;
        for (int i = 0; i < 16; ++i) {
            es.execute(createAccountTask);
        }
        Thread.sleep(1000);
        final long totalAccountsCreated = storage.getAllAccounts()
                .mapToInt(Account::getId)
                .distinct()
                .count();
        Assert.assertEquals(accountsForCreation, totalAccountsCreated);
    }


    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAccountById() {
        storage.removeAccountById(1);
    }
}
