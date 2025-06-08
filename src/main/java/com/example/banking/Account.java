package com.example.banking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank account, managing balance and transactions.
 * Implements Serializable for persistence.
 */
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private double balance;
    private List<Transaction> transactions;
    private transient User owner; // Reference to the owner for persistence updates

    /**
     * Constructor for Account.
     * Initializes balance to 0 and creates a new list for transactions.
     */
    public Account() {
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }
    
    /**
     * Sets the owner of this account.
     * This is used for persistence updates.
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * Deposits a specified amount into the account.
     * @param amount The amount to deposit. Must be positive.
     */
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            transactions.add(new Transaction("Deposit", amount));
            System.out.println("Successfully deposited $" + amount);
            System.out.println("Current Balance: $" + getBalance());
            
            // Notify the user manager to update persistence
            if (owner != null && owner.getUserManager() != null) {
                owner.getUserManager().updateUser(owner);
            }
        } else {
            System.out.println("Deposit amount must be positive.");
        }
    }

    /**
     * Withdraws a specified amount from the account.
     * @param amount The amount to withdraw. Must be positive and not exceed balance.
     */
    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
        } else if (amount > balance) {
            System.out.println("Insufficient funds. Current balance: $" + balance);
        } else {
            balance -= amount;
            transactions.add(new Transaction("Withdrawal", amount));
            System.out.println("Successfully withdrew $" + amount);
            System.out.println("Current Balance: $" + getBalance());
            
            // Notify the user manager to update persistence
            if (owner != null && owner.getUserManager() != null) {
                owner.getUserManager().updateUser(owner);
            }
        }
    }

    /**
     * Gets the current balance of the account.
     * @return The current balance.
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Lists all transactions for this account.
     */
    public void listTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions to display.");
            return;
        }
        
        System.out.println("\n===== Transaction History =====");
        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
        System.out.println("Current Balance: $" + balance);
    }
    
    /**
     * Gets the list of transactions for this account.
     * @return The list of transactions.
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }
}