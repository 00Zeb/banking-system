package com.example.banking.api.service;

import com.example.banking.persistence.FileUserRepository;
import com.example.banking.persistence.UserRepository;
import com.example.banking.user.User;
import com.example.banking.user.UserManager;
import com.example.banking.domain.Account;
import com.example.banking.domain.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer that wraps the banking core functionality.
 * This service delegates all operations to the existing banking application.
 */
@Service
public class BankingService {

    private final UserManager userManager;

    public BankingService() {
        // Initialize the core banking components
        UserRepository repository = new FileUserRepository();
        this.userManager = new UserManager(repository);
    }

    /**
     * Register a new user.
     */
    public boolean registerUser(String username, String password) {
        return userManager.registerUser(username, password);
    }

    /**
     * Authenticate a user and return user information.
     */
    public User authenticateUser(String username, String password) {
        return userManager.authenticateUser(username, password);
    }

    /**
     * Get user by username (for authenticated operations).
     */
    public User getAuthenticatedUser(String username, String password) {
        return userManager.authenticateUser(username, password);
    }

    /**
     * Perform a deposit operation.
     */
    public boolean deposit(String username, String password, double amount) {
        User user = userManager.authenticateUser(username, password);
        if (user != null && amount > 0) {
            user.getAccount().deposit(amount);
            return true;
        }
        return false;
    }

    /**
     * Perform a withdrawal operation.
     */
    public boolean withdraw(String username, String password, double amount) {
        User user = userManager.authenticateUser(username, password);
        if (user != null && amount > 0) {
            Account account = user.getAccount();
            if (account.getBalance() >= amount) {
                account.withdraw(amount);
                return true;
            }
        }
        return false;
    }

    /**
     * Get account balance for a user.
     */
    public Double getBalance(String username, String password) {
        User user = userManager.authenticateUser(username, password);
        return user != null ? user.getAccount().getBalance() : null;
    }

    /**
     * Get transaction history for a user.
     */
    public List<Transaction> getTransactions(String username, String password) {
        User user = userManager.authenticateUser(username, password);
        return user != null ? user.getAccount().getTransactions() : null;
    }

    /**
     * Delete a user account.
     */
    public boolean deleteUser(String username, String password) {
        User user = userManager.authenticateUser(username, password);
        if (user != null) {
            return userManager.deleteUser(username);
        }
        return false;
    }

    /**
     * Save all users (for graceful shutdown).
     */
    public void saveAllUsers() {
        userManager.saveAllUsers();
    }
}
