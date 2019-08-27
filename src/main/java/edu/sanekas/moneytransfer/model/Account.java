package edu.sanekas.moneytransfer.model;

import org.apache.juneau.annotation.BeanIgnore;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Account {

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = reentrantReadWriteLock.readLock();
    private final Lock writeLock = reentrantReadWriteLock.writeLock();

    private final int id;
    private long totalMoney;

    public Account(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public long getTotalMoney() {
        readLock.lock();
        try {
            return totalMoney;
        } finally {
            readLock.unlock();
        }
    }

    public boolean debit(long amount) {
        writeLock.lock();
        try {
            if (amount <= 0) {
                return false;
            } else {
                this.totalMoney = totalMoney + amount;
                return true;
            }
        } finally {
            writeLock.unlock();
        }
    }

    public boolean withdraw(long amount) {
        writeLock.lock();
        try {
            if (totalMoney < amount || amount <= 0) {
                return false;
            } else {
                this.totalMoney = totalMoney - amount;
                return true;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @BeanIgnore
    public Lock getWriteLock() {
        return writeLock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id == account.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
