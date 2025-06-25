package com.example.banking.api.application.service;

import com.example.banking.api.application.port.in.UserAuthenticationUseCase;
import com.example.banking.api.application.port.out.BankingSystemPort;
import com.example.banking.api.domain.model.Account;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application service implementing user authentication use case.
 * Handles user authentication through the external banking system.
 */
@Service
public class UserAuthenticationService implements UserAuthenticationUseCase {
    
    private final BankingSystemPort bankingSystemPort;
    
    public UserAuthenticationService(BankingSystemPort bankingSystemPort) {
        this.bankingSystemPort = bankingSystemPort;
    }
    
    @Override
    public Optional<Account> authenticateUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        if (password == null || password.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return bankingSystemPort.authenticateUser(username.trim(), password);
    }
    
    @Override
    public boolean userExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        // We can check if user exists by attempting authentication with a dummy password
        // In a real system, we might have a dedicated method for this
        return authenticateUser(username, "dummy").isPresent();
    }
}
