package edu.sanekas.moneytransfer.api;

import edu.sanekas.moneytransfer.Main;
import io.undertow.Undertow;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApiTests {
    private final Undertow server = Main.buildUndertow(8080);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Before
    public void configureEnv() {
        server.start();
    }

    @Test
    public void testCreateAccount() throws IOException, InterruptedException {
        final HttpRequest createAccountRequest = HttpRequest
                .newBuilder(URI.create("http://localhost:8080/accounts"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        final String serializedNewAccount = "{\n" +
                        "\t\"id\": 0,\n" +
                        "\t\"totalMoney\": 0\n" +
                        "}";
        final HttpResponse<String> resp = httpClient.send(createAccountRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("Account should be created", 201, resp.statusCode());
        Assert.assertEquals("Created account is invalid", serializedNewAccount, resp.body());
    }

    @Test
    public void testGetAccountById() throws IOException, InterruptedException {
        final HttpRequest getAccountRequest = HttpRequest
                .newBuilder(URI.create("http://localhost:8080/accounts/0"))
                .GET()
                .build();
        final String serializedNewAccount = "{\n" +
                "\t\"id\": 0,\n" +
                "\t\"totalMoney\": 0\n" +
                "}";
        final HttpResponse<String> resp = httpClient.send(getAccountRequest, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("Account should exist", 200, resp.statusCode());
        Assert.assertEquals("Got invalid account", serializedNewAccount, resp.body());
    }

    @After
    public void stopServer() {
        server.stop();
    }
}
