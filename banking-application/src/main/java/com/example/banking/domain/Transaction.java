package com.example.banking.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a banking transaction.
 * Implements Serializable for persistence.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String type;
    private double amount;
    private LocalDateTime timestamp;
    private static final transient DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor for Transaction.
     * @param type The type of transaction (e.g., "Deposit", "Withdrawal").
     * @param amount The amount of the transaction.
     */
    public Transaction(String type, double amount) {
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a string representation of the transaction.
     * @return Formatted string with transaction type and amount.
     */
    @Override
    public String toString() {
        return String.format("[%s] %s: $%.2f", 
            timestamp.format(formatter), 
            type, 
            amount);
    }
}