package com.example.banking.api.application.service;

import com.example.banking.api.application.port.in.UserRegistrationUseCase;
import com.example.banking.api.application.port.out.BankingSystemPort;
import com.example.banking.api.domain.model.User;
import org.springframework.stereotype.Service;

/**
 * Application service implementing user registration use case.
 * Orchestrates the registration process using domain objects and outbound ports.
 */
@Service
public class UserRegistrationService implements UserRegistrationUseCase {
    
    private final BankingSystemPort bankingSystemPort;
    
    public UserRegistrationService(BankingSystemPort bankingSystemPort) {
        this.bankingSystemPort = bankingSystemPort;
    }
    
    @Override
    public boolean registerUser(String username, String password) {
        // Validate input using domain object
        User user;
        try {
            user = new User(username, password);
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation errors
        }
        
        // Delegate to external banking system
        boolean success = bankingSystemPort.registerUser(user);
        
        if (!success) {
            // Check if user already exists (this is a common failure reason)
            // In a real system, we might want more specific error handling
            throw new UserAlreadyExistsException(username);
        }
        
        return success;
    }
}
