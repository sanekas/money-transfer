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
    public void testEmptyStorage() {
        Assert.assertTrue("Storage should be empty", storage.getAllAccounts().isEmpty());
        Assert.assertTrue("Empty storage returned account",
                storage.getAccountById(1).isEmpty());
    }

    @Test
    public void testGetAccountById() {
        final Account acc = storage.createAccount();
        final Optional<Account> foundAcc = storage.getAccountById(0);
        Assert.assertTrue("Account is not found", foundAcc.isPresent());
        Assert.assertEquals("Accounts should be equal", foundAcc.get(), acc);
        Assert.assertEquals("New account should not have money", 0, acc.getTotalMoney());
    }


    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAccountById() {
        storage.removeAccountById(1);
    }
}
