package edu.sanekas.moneytransfer.api;

import edu.sanekas.moneytransfer.api.misc.PathParamsPreprocessor;
import edu.sanekas.moneytransfer.model.Account;
import edu.sanekas.moneytransfer.model.AccountSerializer;
import edu.sanekas.moneytransfer.storages.AccountsStorage;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import jdk.jfr.Label;

import java.nio.ByteBuffer;
import java.util.Optional;

public class AccountsController {

    public static final String GET_ACCOUNT_BY_ID = "/accounts/{accountId}";
    public static final String POST_CREATE_ACCOUNT = "/accounts";
    public static final String GET_ALL_ACCOUNTS = "/accounts";

    private final AccountsStorage accountsStorage;
    private final AccountSerializer accountSerializer;
    private final PathParamsPreprocessor pathParamsPreprocessor;

    public AccountsController(AccountsStorage accountsStorage, AccountSerializer accountSerializer,
                              PathParamsPreprocessor pathParamsPreprocessor) {
        this.accountsStorage = accountsStorage;
        this.accountSerializer = accountSerializer;
        this.pathParamsPreprocessor = pathParamsPreprocessor;
    }

    @Label(value = GET_ACCOUNT_BY_ID)
    public void getAccountById(HttpServerExchange httpServerExchange) {
        final Optional<Account> account = pathParamsPreprocessor.preprocessAccount(httpServerExchange,
                PathParams.ACCOUNT_ID);
        account.flatMap(accountSerializer::serialize)
                .ifPresentOrElse(serializedAccount -> {
                    httpServerExchange.setStatusCode(StatusCodes.OK);
                    httpServerExchange.getResponseSender().send(serializedAccount);
                }, () -> {
                    httpServerExchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
                    httpServerExchange.getResponseSender().send("Fail to serialize requested account," +
                            " more in logs");
                });
    }

    @Label(value = POST_CREATE_ACCOUNT)
    public void createAccount(HttpServerExchange httpServerExchange) {
        final Account account = accountsStorage.createAccount();
        final Optional<ByteBuffer> serizalizedAccount =  accountSerializer.serialize(account);
        serizalizedAccount.ifPresentOrElse(acc -> {
            httpServerExchange.setStatusCode(StatusCodes.CREATED);
            httpServerExchange.getResponseSender().send(acc);
        }, () -> {
            httpServerExchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            httpServerExchange.getResponseSender().send("Fail to serialize account with id: " + account.getId());
        });
    }

    @Label(value = GET_ALL_ACCOUNTS)
    public void getAllAccounts(HttpServerExchange httpServerExchange) {
        accountSerializer.serialize(accountsStorage.getAllAccounts())
                .ifPresentOrElse(acc -> {
                    httpServerExchange.setStatusCode(StatusCodes.OK);
                    httpServerExchange.getResponseSender().send(acc);
                }, () -> {
                    httpServerExchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
                    httpServerExchange.getResponseSender().send("Fail to serialize accounts");
                });
    }
}
