package edu.sanekas.moneytransfer.api;

import edu.sanekas.moneytransfer.api.misc.ErrorMessages;
import edu.sanekas.moneytransfer.api.misc.PathParamExtractor;
import edu.sanekas.moneytransfer.model.Account;
import edu.sanekas.moneytransfer.model.AccountSerializer;
import edu.sanekas.moneytransfer.storages.AccountsStorage;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import jdk.jfr.Label;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

public class AccountsController {

    public static final String GET_ACCOUNT_BY_ID = "/accounts/{accountId}";
    public static final String POST_CREATE_ACCOUNT = "/accounts";

    private final AccountsStorage accountsStorage;
    private final AccountSerializer accountSerializer;

    public AccountsController(AccountsStorage accountsStorage, AccountSerializer accountSerializer) {
        this.accountsStorage = accountsStorage;
        this.accountSerializer = accountSerializer;
    }

    @Label(value = GET_ACCOUNT_BY_ID)
    public void getAccountById(HttpServerExchange httpServerExchange) {
        try {
            final int accountId = PathParamExtractor.extractPathParam(httpServerExchange, PathParams.ACCOUNT_ID)
                    .map(Integer::parseUnsignedInt)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ACCOUNT_ID_IS_UNDEFINED));
            final Account account = accountsStorage.getAccountById(accountId)
                    .orElseThrow(() -> new NoSuchElementException(
                            String.format(ErrorMessages.ACCOUNT_WITH_ID_NOT_FOUND, accountId)));
            final ByteBuffer serializedAccount = accountSerializer.serialize(account);
            httpServerExchange.setStatusCode(StatusCodes.OK);
            httpServerExchange.getResponseSender().send(serializedAccount);
        } catch (IllegalArgumentException e) {
            httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST);
            httpServerExchange.getResponseSender().send(e.getMessage());
        } catch (NoSuchElementException e) {
            httpServerExchange.setStatusCode(StatusCodes.NOT_FOUND);
            httpServerExchange.getResponseSender().send(e.getMessage());
        } catch (Exception e) {
            httpServerExchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            httpServerExchange.getResponseSender().send(e.getMessage());
        }
    }

    @Label(value = POST_CREATE_ACCOUNT)
    public void createAccount(HttpServerExchange httpServerExchange) {
        try {
            final Account account = accountsStorage.createAccount();
            final ByteBuffer serializedAccount = accountSerializer.serialize(account);
            httpServerExchange.setStatusCode(StatusCodes.CREATED);
            httpServerExchange.getResponseSender().send(serializedAccount);
        } catch (Exception e) {
            httpServerExchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            httpServerExchange.getResponseSender().send(e.getMessage());
        }
    }
}
