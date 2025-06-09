package com.example.banking.api.service;

import com.example.banking.api.model.BankingTransaction;
import com.example.banking.api.model.BankingUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer that wraps the banking core functionality.
 * This service delegates all operations to the banking application JAR process
 * using Maven dependency management for JAR location.
 */
@Service
public class BankingService {

    private final BankingProcessService processService;

    @Autowired
    public BankingService(BankingProcessService processService) {
        this.processService = processService;
    }

    /**
     * Register a new user.
     */
    public boolean registerUser(String username, String password) {
        return processService.registerUser(username, password);
    }

    /**
     * Authenticate a user and return user information.
     */
    public BankingUser authenticateUser(String username, String password) {
        return processService.authenticateUser(username, password);
    }

    /**
     * Get user by username (for authenticated operations).
     */
    public BankingUser getAuthenticatedUser(String username, String password) {
        return processService.authenticateUser(username, password);
    }

    /**
     * Perform a deposit operation.
     */
    public boolean deposit(String username, String password, double amount) {
        if (amount <= 0) {
            return false;
        }
        return processService.deposit(username, password, amount);
    }

    /**
     * Perform a withdrawal operation.
     */
    public boolean withdraw(String username, String password, double amount) {
        if (amount <= 0) {
            return false;
        }
        return processService.withdraw(username, password, amount);
    }

    /**
     * Get account balance for a user.
     */
    public Double getBalance(String username, String password) {
        return processService.getBalance(username, password);
    }

    /**
     * Get transaction history for a user.
     */
    public List<BankingTransaction> getTransactions(String username, String password) {
        return processService.getTransactions(username, password);
    }

    /**
     * Delete a user account.
     */
    public boolean deleteUser(String username, String password) {
        return processService.deleteUser(username, password);
    }

    /**
     * Save all users (for graceful shutdown).
     * Note: This is handled automatically by the banking application process.
     */
    public void saveAllUsers() {
        // No action needed - the banking application handles persistence automatically
    }
}
