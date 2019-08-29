package edu.sanekas.moneytransfer.storages;

import edu.sanekas.moneytransfer.model.Account;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Optional;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppendableInMemoryAccountsStorageTest {
    private final AccountsStorage storage = AppendableInMemoryAccountsStorage.S;


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


    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAccountById() {
        storage.removeAccountById(1);
    }
}
