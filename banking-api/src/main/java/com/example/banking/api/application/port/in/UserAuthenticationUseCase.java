package com.example.banking.api.application.port.in;

import com.example.banking.api.domain.model.Account;
import com.example.banking.api.domain.model.User;

import java.util.Optional;

/**
 * Inbound port for user authentication use case.
 * Defines the contract for authenticating users.
 */
public interface UserAuthenticationUseCase {
    
    /**
     * Authenticates a user with username and password.
     * 
     * @param username The username
     * @param password The password
     * @return Optional containing the user's account if authentication successful, empty otherwise
     */
    Optional<Account> authenticateUser(String username, String password);
    
    /**
     * Checks if a user exists in the system.
     * 
     * @param username The username to check
     * @return true if user exists, false otherwise
     */
    boolean userExists(String username);
}
