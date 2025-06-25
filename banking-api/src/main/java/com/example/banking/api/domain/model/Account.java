package com.example.banking.api.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain entity representing a bank account.
 * Contains business logic for account operations.
 */
public class Account {
    
    private final String username;
    private Money balance;
    private final List<Transaction> transactions;
    
    public Account(String username) {
        this(username, Money.zero());
    }
    
    public Account(String username, Money initialBalance) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (initialBalance == null) {
            throw new IllegalArgumentException("Initial balance cannot be null");
        }
        
        this.username = username.trim();
        this.balance = initialBalance;
        this.transactions = new ArrayList<>();
    }
    
    /**
     * Deposits money into the account.
     * @param amount The amount to deposit
     * @return The transaction that was created
     */
    public Transaction deposit(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        this.balance = this.balance.add(amount);
        Transaction transaction = Transaction.deposit(amount, LocalDateTime.now());
        this.transactions.add(transaction);
        
        return transaction;
    }
    
    /**
     * Withdraws money from the account.
     * @param amount The amount to withdraw
     * @return The transaction that was created
     * @throws IllegalArgumentException if insufficient funds
     */
    public Transaction withdraw(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        if (this.balance.isLessThan(amount)) {
            throw new IllegalArgumentException("Insufficient funds. Current balance: " + this.balance + ", Requested: " + amount);
        }
        
        this.balance = this.balance.subtract(amount);
        Transaction transaction = Transaction.withdrawal(amount, LocalDateTime.now());
        this.transactions.add(transaction);
        
        return transaction;
    }
    
    public String getUsername() {
        return username;
    }
    
    public Money getBalance() {
        return balance;
    }
    
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
    
    public List<Transaction> getRecentTransactions(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        
        int fromIndex = Math.max(0, transactions.size() - limit);
        return Collections.unmodifiableList(transactions.subList(fromIndex, transactions.size()));
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(username, account.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", balance=" + balance +
                ", transactionCount=" + transactions.size() +
                '}';
    }
}
