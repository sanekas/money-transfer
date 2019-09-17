package edu.sanekas.moneytransfer.storages;

import edu.sanekas.moneytransfer.model.Account;

import java.util.Optional;
import java.util.stream.Stream;

public class AccountsManager implements AccountsStorage {
    private final AccountsStorage accountsStorage;

    public AccountsManager(AccountsStorage accountsStorage) {
        this.accountsStorage = accountsStorage;
    }

    @Override
    public Optional<Account> getAccountById(int accountId) {
        return accountsStorage.getAccountById(accountId);
    }

    @Override
    public Account createAccount() {
        return accountsStorage.createAccount();
    }

    @Override
    public boolean removeAccountById(int accountId) {
        return accountsStorage.removeAccountById(accountId);
    }

    @Override
    public Stream<Account> getAllAccounts() {
        return accountsStorage.getAllAccounts();
    }
}
