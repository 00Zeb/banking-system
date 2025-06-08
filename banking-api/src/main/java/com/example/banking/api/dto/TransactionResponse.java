package com.example.banking.api.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for transaction information.
 */
public class TransactionResponse {
    
    private String type;
    private double amount;
    private LocalDateTime timestamp;
    private double newBalance;

    public TransactionResponse() {}

    public TransactionResponse(String type, double amount, LocalDateTime timestamp, double newBalance) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.newBalance = newBalance;
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

    public double getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(double newBalance) {
        this.newBalance = newBalance;
    }
}
