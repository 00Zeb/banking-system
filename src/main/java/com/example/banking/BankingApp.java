package com.example.banking;

import com.example.banking.persistence.FileUserRepository;
import com.example.banking.persistence.UserRepository;
import com.example.banking.ui.BankingUI;

/**
 * Main class for the banking application.
 */
public class BankingApp {
    public static void main(String[] args) {
        // Create a file-based repository for persistence
        UserRepository repository = new FileUserRepository();
        
        // Create user manager with the repository
        UserManager userManager = new UserManager(repository);
        
        // Add shutdown hook to save all users when application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Saving all data before exit...");
            userManager.saveAllUsers();
        }));
        
        // Create and start the UI
        BankingUI ui = new BankingUI(userManager);
        ui.start();
    }
}