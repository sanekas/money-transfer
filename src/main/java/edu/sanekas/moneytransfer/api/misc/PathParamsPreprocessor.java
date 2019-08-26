package edu.sanekas.moneytransfer.api.misc;

import edu.sanekas.moneytransfer.model.Account;
import edu.sanekas.moneytransfer.storages.AccountsStorage;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.util.Optional;

public class PathParamsPreprocessor {
    private final AccountsStorage storage;

    public PathParamsPreprocessor(AccountsStorage storage) {
        this.storage = storage;
    }

    public Optional<Account> preprocessAccount(HttpServerExchange httpServerExchange, String accountParamName) {
        final Optional<String> strAccountId = PathParamExtractor.extractPathParam(httpServerExchange, accountParamName);
        if (strAccountId.isEmpty()) {
            httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST);
            httpServerExchange.getResponseSender().send(accountParamName + " is undefined");
            return Optional.empty();
        }
        final Optional<Integer> intAccountId = strAccountId.flatMap(accountIdStr -> {
            try {
                return Optional.of(Integer.parseUnsignedInt(accountIdStr));
            } catch (NumberFormatException e) {
                System.err.println("Fail to parse " + accountParamName + " with id " + accountIdStr);
                return Optional.empty();
            }
        });
        if (intAccountId.isEmpty()) {
            httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST);
            httpServerExchange.getResponseSender().send("Unable to parse accountId: " + strAccountId.get());
            return Optional.empty();
        }
        final Optional<Account> account = intAccountId.flatMap(storage::getAccountById);
        if (account.isEmpty()) {
            httpServerExchange.setStatusCode(StatusCodes.NOT_FOUND);
            httpServerExchange.getResponseSender().send("No account with id: " + intAccountId.get());
            return Optional.empty();
        }
        return account;
    }

    public Optional<Long> preprocessAmount(HttpServerExchange httpServerExchange, String amountParamName) {
        return PathParamExtractor.extractPathParam(httpServerExchange, amountParamName)
                .flatMap(amount -> {
                    try {
                        return Optional.of(Long.parseLong(amount));
                    } catch (NumberFormatException e) {
                        httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST);
                        httpServerExchange.getResponseSender().send("Fail to cast amount to long");
                        return Optional.empty();
                    }
                });
    }
}
