package com.example.banking.api.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing a banking transaction.
 * Immutable value object that captures transaction details.
 */
public class Transaction {
    
    public enum Type {
        DEPOSIT, WITHDRAWAL
    }
    
    private final Type type;
    private final Money amount;
    private final LocalDateTime timestamp;
    private final String description;
    
    public Transaction(Type type, Money amount, LocalDateTime timestamp, String description) {
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Transaction amount cannot be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Transaction timestamp cannot be null");
        }
        
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description != null ? description : "";
    }
    
    public Transaction(Type type, Money amount, LocalDateTime timestamp) {
        this(type, amount, timestamp, null);
    }
    
    public static Transaction deposit(Money amount, LocalDateTime timestamp) {
        return new Transaction(Type.DEPOSIT, amount, timestamp, "Deposit");
    }
    
    public static Transaction withdrawal(Money amount, LocalDateTime timestamp) {
        return new Transaction(Type.WITHDRAWAL, amount, timestamp, "Withdrawal");
    }
    
    public Type getType() {
        return type;
    }
    
    public Money getAmount() {
        return amount;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isDeposit() {
        return type == Type.DEPOSIT;
    }
    
    public boolean isWithdrawal() {
        return type == Type.WITHDRAWAL;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return type == that.type &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, amount, timestamp, description);
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "type=" + type +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                '}';
    }
}
