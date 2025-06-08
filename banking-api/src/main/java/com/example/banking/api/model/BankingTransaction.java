package com.example.banking.api.model;

import java.time.LocalDateTime;

/**
 * Internal model representing a banking transaction.
 * Used to replace the original Transaction class from banking-application.
 */
public class BankingTransaction {
    
    private String type;
    private double amount;
    private LocalDateTime timestamp;
    
    public BankingTransaction() {}
    
    public BankingTransaction(String type, double amount, LocalDateTime timestamp) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
