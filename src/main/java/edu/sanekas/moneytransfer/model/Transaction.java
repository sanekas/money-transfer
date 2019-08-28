package edu.sanekas.moneytransfer.model;

public class Transaction {
    private final Account fromAccount;
    private final Account toAccount;
    private final long amount;

    public Transaction(Account fromAccount, Account toAccount, long amount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }

    public boolean execute() {
        final int fromAccId = fromAccount.getId();
        final int toAccId = toAccount.getId();
        if (fromAccId == toAccId) {
            return false;
        }
        if (fromAccId < toAccId) {
            fromAccount.getWriteLock().lock();
            try {
                toAccount.getWriteLock().lock();
                try {
                    return withdrawAndDebit(fromAccount, toAccount, amount);
                } finally {
                    toAccount.getWriteLock().unlock();
                }
            } finally {
                fromAccount.getWriteLock().unlock();
            }
        } else {
            toAccount.getWriteLock().lock();
            try {
                fromAccount.getWriteLock().lock();
                try {
                    return withdrawAndDebit(fromAccount, toAccount, amount);
                } finally {
                    fromAccount.getWriteLock().unlock();
                }
            } finally {
                toAccount.getWriteLock().unlock();
            }
        }
    }

    private static boolean withdrawAndDebit(Account fromAccount, Account toAccount, long amount) {
        final boolean isWithdrawSuccessful = fromAccount.withdraw(amount);
        if (isWithdrawSuccessful) {
            final boolean isDebitSuccessful = toAccount.debit(amount);
            if (isDebitSuccessful) {
                return true;
            } else {
                /**
                 * Restore fromAccount.
                 * Believe that fromAccount is available, because of withdrawing was successful.
                 * There is much to talk.
                 */
                fromAccount.debit(amount);
                return false;
            }
        }
        return false;
    }
}
