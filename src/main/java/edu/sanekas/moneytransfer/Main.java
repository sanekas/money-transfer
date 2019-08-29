package edu.sanekas.moneytransfer;

import edu.sanekas.moneytransfer.api.AccountsController;
import edu.sanekas.moneytransfer.api.FinanceOperationsController;
import edu.sanekas.moneytransfer.api.misc.PathParamsPreprocessor;
import edu.sanekas.moneytransfer.model.JsonAccountSerializer;
import edu.sanekas.moneytransfer.storages.AccountsStorage;
import edu.sanekas.moneytransfer.storages.AppendableInMemoryAccountsStorage;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;

public class Main {
    public static void main(String[] args) {
        final int port = 8080;
        final Undertow undertow = buildUndertow(port);
        undertow.start();
        System.out.println("Application started at port: " + port);
    }

    public static Undertow buildUndertow(int port) {
        final AccountsStorage accountsStorage = new AppendableInMemoryAccountsStorage();

        final PathParamsPreprocessor pathParamsPreprocessor =
                new PathParamsPreprocessor(accountsStorage);

        final AccountsController accountsController =
                new AccountsController(accountsStorage, JsonAccountSerializer.S,
                        pathParamsPreprocessor);

        final FinanceOperationsController financeOperationsController =
                new FinanceOperationsController(pathParamsPreprocessor,
                        JsonAccountSerializer.S);

        final RoutingHandler handler = Handlers.routing()
                .get(AccountsController.GET_ACCOUNT_BY_ID, accountsController::getAccountById)
                .get(AccountsController.GET_ALL_ACCOUNTS, accountsController::getAllAccounts)
                .post(AccountsController.POST_CREATE_ACCOUNT, accountsController::createAccount)
                .put(FinanceOperationsController.PUT_DEBIT_TO_ACCOUNT, financeOperationsController::debitToAccount)
                .put(FinanceOperationsController.PUT_WITHDRAW_FROM_ACCOUNT,
                        financeOperationsController::withdrawFromAccount)
                .put(FinanceOperationsController.PUT_TRANSFER, financeOperationsController::makeTransfer);

        return Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(handler)
                .build();
    }
}
