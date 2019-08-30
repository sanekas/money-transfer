package edu.sanekas.moneytransfer.api;

import edu.sanekas.moneytransfer.model.Account;
import edu.sanekas.moneytransfer.model.JsonAccountSerializer;
import edu.sanekas.moneytransfer.storages.AccountsStorage;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import org.apache.juneau.parser.ParseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApiTests {
    private static final AccountsStorage accountsStorage = Mockito.mock(AccountsStorage.class);

    private static final AccountsController accountsController =
            new AccountsController(accountsStorage, JsonAccountSerializer.S);

    private static final FinanceOperationsController financeOperationsController =
            new FinanceOperationsController(JsonAccountSerializer.S, accountsStorage);

    private static final RoutingHandler handler = Handlers.routing()
            .get(AccountsController.GET_ACCOUNT_BY_ID, accountsController::getAccountById)
            .post(AccountsController.POST_CREATE_ACCOUNT, accountsController::createAccount)
            .put(FinanceOperationsController.PUT_DEBIT_TO_ACCOUNT, financeOperationsController::debitToAccount)
            .put(FinanceOperationsController.PUT_WITHDRAW_FROM_ACCOUNT,
                    financeOperationsController::withdrawFromAccount)
            .put(FinanceOperationsController.PUT_TRANSFER, financeOperationsController::makeTransfer);

    private static Undertow undertow = Undertow.builder()
            .addHttpListener(8080, "localhost")
            .setHandler(handler)
            .build();

    private static HttpClient httpClient = HttpClient.newHttpClient();

    @Before
    public void configureEnv() {
        undertow.start();
    }

    @Test
    public void testCreateAccount() throws IOException, InterruptedException, ParseException {
        Mockito.when(accountsStorage.createAccount()).thenReturn(new Account(0));
        final HttpRequest createAccountRequest = HttpRequest
                .newBuilder(URI.create("http://localhost:8080/accounts"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        final String res = "{\"id\":0,\"totalMoney\":0}";
        final HttpResponse<String> resp = httpClient.send(createAccountRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(res, resp.body());
    }

    @Test
    public void testGetAccountById() throws IOException, InterruptedException {
        Mockito.when(accountsStorage.getAccountById(0)).thenReturn(Optional.of(new Account(0)));
        final HttpRequest getAccountRequest = HttpRequest
                .newBuilder(URI.create("http://localhost:8080/accounts/0"))
                .GET()
                .build();
        final String res = "{\"id\":0,\"totalMoney\":0}";
        final HttpResponse<String> resp = httpClient.send(getAccountRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("Account should exist", 200, resp.statusCode());
        Assert.assertEquals("Got invalid account", res, resp.body());
    }

    @After
    public void stopServer() {
        undertow.stop();
    }
}
