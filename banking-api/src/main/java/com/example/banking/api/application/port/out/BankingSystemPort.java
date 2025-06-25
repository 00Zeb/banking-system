package com.example.banking.api.application.port.out;

import com.example.banking.api.domain.model.Account;
import com.example.banking.api.domain.model.Money;
import com.example.banking.api.domain.model.Transaction;
import com.example.banking.api.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for external banking system operations.
 * This port abstracts the communication with the external banking application.
 */
public interface BankingSystemPort {
    
    /**
     * Registers a new user in the external banking system.
     * 
     * @param user The user to register
     * @return true if registration was successful, false otherwise
     */
    boolean registerUser(User user);
    
    /**
     * Authenticates a user with the external banking system.
     * 
     * @param username The username
     * @param password The password
     * @return Optional containing the account if authentication successful, empty otherwise
     */
    Optional<Account> authenticateUser(String username, String password);
    
    /**
     * Performs a deposit operation in the external banking system.
     * 
     * @param username The username
     * @param password The password for authentication
     * @param amount The amount to deposit
     * @return true if deposit was successful, false otherwise
     */
    boolean deposit(String username, String password, Money amount);
    
    /**
     * Performs a withdrawal operation in the external banking system.
     * 
     * @param username The username
     * @param password The password for authentication
     * @param amount The amount to withdraw
     * @return true if withdrawal was successful, false otherwise
     */
    boolean withdraw(String username, String password, Money amount);
    
    /**
     * Gets the current balance from the external banking system.
     * 
     * @param username The username
     * @param password The password for authentication
     * @return Optional containing the balance if successful, empty otherwise
     */
    Optional<Money> getBalance(String username, String password);
    
    /**
     * Gets transaction history from the external banking system.
     * 
     * @param username The username
     * @param password The password for authentication
     * @return Optional containing the list of transactions if successful, empty otherwise
     */
    Optional<List<Transaction>> getTransactionHistory(String username, String password);
    
    /**
     * Deletes a user from the external banking system.
     * 
     * @param username The username
     * @param password The password for authentication
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteUser(String username, String password);
}
