package com.example.banking.api.dto;

import jakarta.validation.constraints.Positive;

/**
 * Request DTO for banking transactions using session-based authentication.
 * This replaces TransactionRequest for session-based operations.
 */
public class SessionTransactionRequest {
    
    @Positive(message = "Amount must be positive")
    private double amount;

    public SessionTransactionRequest() {}

    public SessionTransactionRequest(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}