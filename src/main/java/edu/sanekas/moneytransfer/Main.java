package edu.sanekas.moneytransfer;

import edu.sanekas.moneytransfer.api.AccountsController;
import edu.sanekas.moneytransfer.api.FinanceOperationsController;
import edu.sanekas.moneytransfer.model.JsonAccountSerializer;
import edu.sanekas.moneytransfer.storages.AccountsStorage;
import edu.sanekas.moneytransfer.storages.AppendableInMemoryAccountsStorage;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;

public class Main {
    private final static int PORT = 8080;

    public static void main(String[] args) {
        final AccountsStorage accountsStorage = new AppendableInMemoryAccountsStorage();

        final AccountsController accountsController =
                new AccountsController(accountsStorage, JsonAccountSerializer.S);

        final FinanceOperationsController financeOperationsController =
                new FinanceOperationsController(JsonAccountSerializer.S, accountsStorage);

        final RoutingHandler handler = Handlers.routing()
                .get(AccountsController.GET_ACCOUNT_BY_ID, accountsController::getAccountById)
                .post(AccountsController.POST_CREATE_ACCOUNT, accountsController::createAccount)
                .put(FinanceOperationsController.PUT_DEBIT_TO_ACCOUNT, financeOperationsController::debitToAccount)
                .put(FinanceOperationsController.PUT_WITHDRAW_FROM_ACCOUNT,
                        financeOperationsController::withdrawFromAccount)
                .put(FinanceOperationsController.PUT_TRANSFER, financeOperationsController::makeTransfer);

        final Undertow undertow = Undertow.builder()
                .addHttpListener(PORT, "localhost")
                .setHandler(handler)
                .build();
        undertow.start();
        System.out.println("Application started at port: " + PORT);
    }
}
