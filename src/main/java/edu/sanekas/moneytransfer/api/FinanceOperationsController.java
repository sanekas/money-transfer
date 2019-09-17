package edu.sanekas.moneytransfer.api;

import edu.sanekas.moneytransfer.api.misc.ErrorMessages;
import edu.sanekas.moneytransfer.api.misc.PathParamExtractor;
import edu.sanekas.moneytransfer.model.Account;
import edu.sanekas.moneytransfer.model.AccountSerializer;
import edu.sanekas.moneytransfer.model.Transaction;
import edu.sanekas.moneytransfer.storages.AccountsManager;
import edu.sanekas.moneytransfer.storages.AccountsStorage;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import jdk.jfr.Label;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

public class FinanceOperationsController {
    public static final String PUT_DEBIT_TO_ACCOUNT = "/accounts/{accountId}/debit/{amount}";
    public static final String PUT_WITHDRAW_FROM_ACCOUNT = "/accounts/{accountId}/withdraw/{amount}";
    public static final String POST_TRANSFER = "/accounts/from/{fromAccountId}/to/{toAccountId}/transfer/{amount}";

    private final AccountSerializer accountSerializer;
    private final AccountsManager accountsManager;

    public FinanceOperationsController(AccountSerializer accountSerializer, AccountsManager accountsManager) {
        this.accountSerializer = accountSerializer;
        this.accountsManager = accountsManager;
    }

    @Label(value = PUT_DEBIT_TO_ACCOUNT)
    public void debitToAccount(HttpServerExchange httpServerExchange) {
        try {
            final int accountId = PathParamExtractor.extractPathParam(httpServerExchange, PathParams.ACCOUNT_ID)
                    .map(Integer::parseUnsignedInt)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ACCOUNT_ID_IS_UNDEFINED));
            final Account account = accountsManager.getAccountById(accountId)
                    .orElseThrow(() -> new NoSuchElementException(
                            String.format(ErrorMessages.ACCOUNT_WITH_ID_NOT_FOUND, accountId)));
            final long amount = PathParamExtractor.extractPathParam(httpServerExchange, PathParams.AMOUNT)
                    .map(Long::parseUnsignedLong)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.AMOUNT_IS_UNDEFINED));
            final boolean isDebitSuccessful = account.debit(amount);
            if (isDebitSuccessful) {
                httpServerExchange.setStatusCode(StatusCodes.OK);
                final ByteBuffer serializedAccount = accountSerializer.serialize(account);
                httpServerExchange.getResponseSender().send(serializedAccount);
            } else {
                httpServerExchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
                httpServerExchange.getResponseSender().send("Debit for accountId: " + accountId + " with amount: "
                        + amount + " is unsuccessful");
            }
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

    @Label(value = PUT_WITHDRAW_FROM_ACCOUNT)
    public void withdrawFromAccount(HttpServerExchange httpServerExchange) {
        try {
            final int accountId = PathParamExtractor.extractPathParam(httpServerExchange, PathParams.ACCOUNT_ID)
                    .map(Integer::parseUnsignedInt)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ACCOUNT_ID_IS_UNDEFINED));
            final Account account = accountsManager.getAccountById(accountId)
                    .orElseThrow(() -> new NoSuchElementException(
                            String.format(ErrorMessages.ACCOUNT_WITH_ID_NOT_FOUND, accountId)));
            final long amount = PathParamExtractor.extractPathParam(httpServerExchange, PathParams.AMOUNT)
                    .map(Long::parseUnsignedLong)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.AMOUNT_IS_UNDEFINED));
            final boolean isWithdrawingSuccessful = account.withdraw(amount);
            if (isWithdrawingSuccessful) {
                httpServerExchange.setStatusCode(StatusCodes.OK);
                final ByteBuffer serializedAccount = accountSerializer.serialize(account);
                httpServerExchange.getResponseSender().send(serializedAccount);
            } else {
                httpServerExchange.setStatusCode(StatusCodes.UNPROCESSABLE_ENTITY);
                httpServerExchange.getResponseSender().send("Withdrawing for accountId: " + accountId +
                        " with amount: " + amount + " is unsuccessful, not enough money");
            }
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

    @Label(value = POST_TRANSFER)
    public void makeTransfer(HttpServerExchange httpServerExchange) {
        try {
            final int fromAccountId = PathParamExtractor.extractPathParam(httpServerExchange, PathParams.FROM_ACCOUNT_ID)
                    .map(Integer::parseUnsignedInt)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ACCOUNT_ID_IS_UNDEFINED));
            final Account fromAccount = accountsManager.getAccountById(fromAccountId)
                    .orElseThrow(() -> new NoSuchElementException(
                            String.format(ErrorMessages.ACCOUNT_WITH_ID_NOT_FOUND, fromAccountId)));
            final int toAccountId = PathParamExtractor.extractPathParam(httpServerExchange, PathParams.TO_ACCOUNT_ID)
                    .map(Integer::parseUnsignedInt)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ACCOUNT_ID_IS_UNDEFINED));
            final Account toAccount = accountsManager.getAccountById(toAccountId)
                    .orElseThrow(() -> new NoSuchElementException(
                            String.format(ErrorMessages.ACCOUNT_WITH_ID_NOT_FOUND, fromAccountId)));
            final long amount = PathParamExtractor.extractPathParam(httpServerExchange, PathParams.AMOUNT)
                    .map(Long::parseUnsignedLong)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.AMOUNT_IS_UNDEFINED));
            final boolean isTransactionSuccessful = new Transaction(fromAccount, toAccount, amount).execute();
            if (isTransactionSuccessful) {
                final ByteBuffer serializedAccount = accountSerializer.serialize(fromAccount);
                httpServerExchange.setStatusCode(StatusCodes.OK);
                httpServerExchange.getResponseSender().send(serializedAccount);
            } else {
                httpServerExchange.setStatusCode(StatusCodes.UNPROCESSABLE_ENTITY);
                httpServerExchange.getResponseSender().send("Transaction is failed, fromAccId: " +
                        fromAccount.getId() + " toAccId: " + toAccount.getId() + " amount: " + amount);
            }
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
}
