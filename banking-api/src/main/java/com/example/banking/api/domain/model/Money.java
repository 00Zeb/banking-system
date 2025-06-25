package com.example.banking.api.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing money amounts.
 * Ensures proper handling of monetary values with precision.
 */
public class Money {
    
    private final BigDecimal amount;
    
    public Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
    
    public Money(double amount) {
        this(BigDecimal.valueOf(amount));
    }
    
    public Money(String amount) {
        this(new BigDecimal(amount));
    }
    
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }
    
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
    
    public Money subtract(Money other) {
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        return new Money(result);
    }
    
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }
    
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }
    
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public double toDouble() {
        return amount.doubleValue();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
    
    @Override
    public String toString() {
        return "$" + amount.toString();
    }
}
