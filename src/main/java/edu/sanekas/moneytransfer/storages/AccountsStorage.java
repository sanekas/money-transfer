package edu.sanekas.moneytransfer.storages;

import edu.sanekas.moneytransfer.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface for accounts storage
 */
public interface AccountsStorage {
    /**
     * @param accountId
     * @return bank account by unique identifier
     */
    Optional<Account> getAccountById(int accountId);

    /**
     * Creates new account
     * @return new account
     */
    Account createAccount();

    /**
     * @param accountId
     * @return true if account is found and successfully deleted, else - false
     */
    boolean removeAccountById(int accountId);

    /**
     * @return stream with all accounts
     */
    Stream<Account> getAllAccounts();
}
