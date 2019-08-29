package edu.sanekas.moneytransfer.storages;

import edu.sanekas.moneytransfer.model.Account;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Appendable in memory storage for accounts.
 * Removement account by unique identifier is not supported.
 */
public class AppendableInMemoryAccountsStorage implements AccountsStorage {

    private final List<Account> accounts = new CopyOnWriteArrayList<>();

    @Override
    public Optional<Account> getAccountById(int accountId) {
        final int currentSize = accounts.size();
        if (accountId >= currentSize || accountId < 0) {
            return Optional.empty();
        } else {
            return Optional.of(accounts.get(accountId));
        }
    }

    @Override
    public Account createAccount() {
        final int newAccountId = accounts.size();
        final Account newAccount = new Account(newAccountId);
        accounts.add(newAccount);
        return newAccount;
    }

    @Override
    public boolean removeAccountById(int accountId) {
        throw new UnsupportedOperationException("Storage is just appendable");
    }
}
