package com.example.banking.api.domain;

import java.util.List;

/**
 * Internal model representing a banking user.
 * Used to replace the original User class from banking-application.
 */
public class BankingUser {
    
    private String username;
    private double balance;
    private List<com.example.banking.api.domain.BankingTransaction> transactions;
    
    public BankingUser() {}
    
    public BankingUser(String username, double balance) {
        this.username = username;
        this.balance = balance;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public List<BankingTransaction> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<BankingTransaction> transactions) {
        this.transactions = transactions;
    }
}
