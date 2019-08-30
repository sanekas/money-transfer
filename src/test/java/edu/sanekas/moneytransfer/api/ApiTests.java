package edu.sanekas.moneytransfer.api;

import edu.sanekas.moneytransfer.model.Account;
import edu.sanekas.moneytransfer.model.JsonAccountSerializer;
import edu.sanekas.moneytransfer.storages.AccountsStorage;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import io.undertow.util.StatusCodes;
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
        Assert.assertEquals("Account should exist", StatusCodes.CREATED, resp.statusCode());
        Assert.assertEquals("Got invalid account", res, resp.body());
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
        Assert.assertEquals("Account should exist", StatusCodes.OK, resp.statusCode());
        Assert.assertEquals("Got invalid account", res, resp.body());
    }

    @Test
    public void testNonExistedAccount() throws IOException, InterruptedException {
        Mockito.when(accountsStorage.getAccountById(0)).thenReturn(Optional.empty());
        final HttpRequest getAccountRequest = HttpRequest
                .newBuilder(URI.create("http://localhost:8080/accounts/0"))
                .GET()
                .build();
        final HttpResponse<String> resp = httpClient.send(getAccountRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("Account should not be found", StatusCodes.NOT_FOUND, resp.statusCode());
    }

    @Test
    public void testGetAccountInvalidRequest() throws IOException, InterruptedException {
        final HttpRequest getAccountRequest = HttpRequest
                .newBuilder(URI.create("http://localhost:8080/accounts/abc"))
                .GET()
                .build();
        final HttpResponse<String> resp = httpClient.send(getAccountRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("Request is invalid", StatusCodes.BAD_REQUEST, resp.statusCode());
    }

    @Test
    public void testDebitToAccount() throws IOException, InterruptedException {
        Mockito.when(accountsStorage.getAccountById(0)).thenReturn(Optional.of(new Account(0)));
        final HttpRequest putAccountRequest = HttpRequest
                .newBuilder(URI.create("http://localhost:8080/accounts/0/debit/1000"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        final String res = "{\"id\":0,\"totalMoney\":1000}";
        final HttpResponse<String> resp = httpClient.send(putAccountRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("Request is invalid", StatusCodes.OK, resp.statusCode());
        Assert.assertEquals("Debited account with 1000", res, resp.body());
    }

    @Test
    public void testDebitToInexistedAccount() throws IOException, InterruptedException {
        Mockito.when(accountsStorage.getAccountById(0)).thenReturn(Optional.empty());
        final HttpRequest putAccountRequest = HttpRequest
                .newBuilder(URI.create("http://localhost:8080/accounts/0/debit/1000"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        final HttpResponse<String> resp = httpClient.send(putAccountRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("Account doesn't exist", StatusCodes.NOT_FOUND, resp.statusCode());
    }

    @Test
    public void testDebitNegative() throws IOException, InterruptedException {
        Mockito.when(accountsStorage.getAccountById(0)).thenReturn(Optional.of(new Account(0)));
        final HttpRequest putAccountRequest = HttpRequest
                .newBuilder(URI.create("http://localhost:8080/accounts/0/debit/-1000"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        final HttpResponse<String> resp = httpClient.send(putAccountRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("Account doesn't exist", StatusCodes.BAD_REQUEST, resp.statusCode());
    }

    @Test
    public void testWithdrawFromAccount() throws IOException, InterruptedException {
        final Account acc = new Account(0);
        acc.debit(1000);
        Mockito.when(accountsStorage.getAccountById(0)).thenReturn(Optional.of(acc));
        final HttpRequest putAccountRequest = HttpRequest
                .newBuilder(URI.create("http://localhost:8080/accounts/0/withdraw/300"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        final String res = "{\"id\":0,\"totalMoney\":700}";
        final HttpResponse<String> resp = httpClient.send(putAccountRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("Request is invalid", StatusCodes.OK, resp.statusCode());
        Assert.assertEquals("Account should be with 700", res, resp.body());
    }

    @After
    public void stopServer() {
        undertow.stop();
    }
}
