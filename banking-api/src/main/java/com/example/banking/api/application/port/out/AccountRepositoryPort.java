package com.example.banking.api.application.port.out;

import com.example.banking.api.domain.model.Account;
import com.example.banking.api.domain.model.Money;
import com.example.banking.api.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for account repository operations.
 * Defines the contract for persisting and retrieving account data.
 */
public interface AccountRepositoryPort {
    
    /**
     * Saves an account to the repository.
     * 
     * @param account The account to save
     * @return true if save was successful, false otherwise
     */
    boolean saveAccount(Account account);
    
    /**
     * Finds an account by username.
     * 
     * @param username The username to search for
     * @return Optional containing the account if found, empty otherwise
     */
    Optional<Account> findAccountByUsername(String username);
    
    /**
     * Gets the current balance for a user.
     * 
     * @param username The username
     * @return Optional containing the balance if account found, empty otherwise
     */
    Optional<Money> getBalance(String username);
    
    /**
     * Gets transaction history for a user.
     * 
     * @param username The username
     * @return Optional containing the list of transactions if account found, empty otherwise
     */
    Optional<List<Transaction>> getTransactionHistory(String username);
    
    /**
     * Performs a deposit operation.
     * 
     * @param username The username
     * @param amount The amount to deposit
     * @return true if deposit was successful, false otherwise
     */
    boolean deposit(String username, Money amount);
    
    /**
     * Performs a withdrawal operation.
     * 
     * @param username The username
     * @param amount The amount to withdraw
     * @return true if withdrawal was successful, false if insufficient funds or other error
     */
    boolean withdraw(String username, Money amount);
    
    /**
     * Deletes an account from the repository.
     * 
     * @param username The username of the account to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteAccount(String username);
}
