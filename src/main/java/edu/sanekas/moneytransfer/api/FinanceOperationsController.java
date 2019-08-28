package edu.sanekas.moneytransfer.api;

import edu.sanekas.moneytransfer.api.misc.PathParamsPreprocessor;
import edu.sanekas.moneytransfer.model.Account;
import edu.sanekas.moneytransfer.model.AccountSerializer;
import edu.sanekas.moneytransfer.model.Transaction;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import jdk.jfr.Label;

import java.util.Optional;

public class FinanceOperationsController {
    public static final String PUT_DEBIT_TO_ACCOUNT = "/accounts/{accountId}/debit/{amount}";
    public static final String PUT_WITHDRAW_FROM_ACCOUNT = "/accounts/{accountId}/withdraw/{amount}";
    public static final String PUT_TRANSFER = "/accounts/from/{fromAccountId}/to/{toAccountId}/transfer/{amount}";

    private final PathParamsPreprocessor pathParamsPreprocessor;
    private final AccountSerializer accountSerializer;

    public FinanceOperationsController(PathParamsPreprocessor pathParamsPreprocessor,
                                       AccountSerializer accountSerializer) {
        this.pathParamsPreprocessor = pathParamsPreprocessor;
        this.accountSerializer = accountSerializer;
    }

    @Label(value = PUT_DEBIT_TO_ACCOUNT)
    public void debitToAccount(HttpServerExchange httpServerExchange) {
        final Optional<Account> account = pathParamsPreprocessor.preprocessAccount(httpServerExchange,
                PathParams.ACCOUNT_ID);
        final Optional<Long> amountForDebit = pathParamsPreprocessor.preprocessAmount(httpServerExchange,
                PathParams.AMOUNT);
        account.ifPresent(acc ->
                amountForDebit.ifPresent(amount -> {
                    final boolean isDebitSuccessful = acc.debit(amount);
                    if (isDebitSuccessful) {
                        httpServerExchange.setStatusCode(StatusCodes.OK);
                        accountSerializer.serialize(acc)
                                .ifPresentOrElse(serializedAccount -> {
                                    httpServerExchange.setStatusCode(StatusCodes.OK);
                                    httpServerExchange.getResponseSender().send(serializedAccount);
                                }, () -> new SerializationErrorHandler(httpServerExchange).run());
                    } else {
                        httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST);
                        httpServerExchange.getResponseSender().send("Debit is unsuccessful");
                    }

                }));
    }

    @Label(value = PUT_WITHDRAW_FROM_ACCOUNT)
    public void withdrawFromAccount(HttpServerExchange httpServerExchange) {
        final Optional<Account> account = pathParamsPreprocessor.preprocessAccount(httpServerExchange,
                PathParams.ACCOUNT_ID);
        final Optional<Long> amountForWithdraw = pathParamsPreprocessor.preprocessAmount(httpServerExchange,
                PathParams.AMOUNT);
        account.ifPresent(acc -> amountForWithdraw.ifPresent(amount -> {
            final boolean isWithdrawSuccessful = acc.withdraw(amount);
            if (isWithdrawSuccessful) {
                httpServerExchange.setStatusCode(StatusCodes.OK);
                accountSerializer.serialize(acc)
                        .ifPresentOrElse(serializedAccount -> {
                            httpServerExchange.setStatusCode(StatusCodes.OK);
                            httpServerExchange.getResponseSender().send(serializedAccount);
                        }, () -> new SerializationErrorHandler(httpServerExchange).run());
            } else {
                httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST);
                httpServerExchange.getResponseSender().send("Not enough money at account with id or ampunt <= 0: "
                        + acc.getId());
            }
        }));
    }

    @Label(value = PUT_TRANSFER)
    public void makeTransfer(HttpServerExchange httpServerExchange) {
        final Optional<Account> fromAccount = pathParamsPreprocessor.preprocessAccount(httpServerExchange,
                PathParams.FROM_ACCOUNT_ID);
        fromAccount.ifPresent(fromAcc -> {
            final Optional<Account> toAccount = pathParamsPreprocessor.preprocessAccount(httpServerExchange,
                    PathParams.TO_ACCOUNT_ID);
            toAccount.ifPresent(toAcc -> {
                final Optional<Long> amount = pathParamsPreprocessor.preprocessAmount(httpServerExchange,
                        PathParams.AMOUNT);
                amount.ifPresent(moneyToTransfer -> {
                    final boolean isTransactionSuccessful = new Transaction(fromAcc, toAcc, moneyToTransfer).execute();
                    if (isTransactionSuccessful) {
                        httpServerExchange.setStatusCode(StatusCodes.OK);
                        accountSerializer.serialize(fromAcc)
                                .ifPresentOrElse(serializedAccount -> {
                                    httpServerExchange.setStatusCode(StatusCodes.OK);
                                    httpServerExchange.getResponseSender().send(serializedAccount);
                                }, () -> new SerializationErrorHandler(httpServerExchange).run());
                    } else {
                        httpServerExchange.setStatusCode(StatusCodes.BAD_REQUEST);
                        httpServerExchange.getResponseSender().send("Transaction is failed, fromAccId: " +
                                        fromAcc.getId() + " toAccId: " + toAcc.getId() + " amount: " + amount);
                    }
                });
            });
        });
    }

    private static class SerializationErrorHandler implements Runnable {
        private final HttpServerExchange httpServerExchange;

        SerializationErrorHandler(HttpServerExchange httpServerExchange) {
            this.httpServerExchange = httpServerExchange;
        }

        @Override
        public void run() {
            httpServerExchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            httpServerExchange.getResponseSender().send("Fail to serialize requested account, more in logs");
        }
    }
}
