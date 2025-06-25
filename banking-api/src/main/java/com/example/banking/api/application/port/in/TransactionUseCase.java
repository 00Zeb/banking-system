package com.example.banking.api.application.port.in;

import com.example.banking.api.domain.model.Account;
import com.example.banking.api.domain.model.Money;
import com.example.banking.api.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Inbound port for transaction-related use cases.
 * Defines contracts for deposit, withdrawal, and transaction history operations.
 */
public interface TransactionUseCase {
    
    /**
     * Deposits money into a user's account.
     * 
     * @param username The username
     * @param password The password for authentication
     * @param amount The amount to deposit
     * @return Optional containing the transaction if successful, empty if authentication failed
     * @throws IllegalArgumentException if amount is invalid
     */
    Optional<Transaction> deposit(String username, String password, Money amount);
    
    /**
     * Withdraws money from a user's account.
     * 
     * @param username The username
     * @param password The password for authentication
     * @param amount The amount to withdraw
     * @return Optional containing the transaction if successful, empty if authentication failed or insufficient funds
     * @throws IllegalArgumentException if amount is invalid
     * @throws InsufficientFundsException if account has insufficient funds
     */
    Optional<Transaction> withdraw(String username, String password, Money amount);
    
    /**
     * Gets the current account balance for a user.
     * 
     * @param username The username
     * @param password The password for authentication
     * @return Optional containing the account if authentication successful, empty otherwise
     */
    Optional<Account> getAccount(String username, String password);
    
    /**
     * Gets transaction history for a user.
     * 
     * @param username The username
     * @param password The password for authentication
     * @return Optional containing the list of transactions if authentication successful, empty otherwise
     */
    Optional<List<Transaction>> getTransactionHistory(String username, String password);
    
    /**
     * Exception thrown when attempting to withdraw more money than available.
     */
    class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }
}
