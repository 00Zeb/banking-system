package com.example.banking;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a banking transaction.
 */
public class Transaction {
    private String type;
    private double amount;
    private LocalDateTime timestamp;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
