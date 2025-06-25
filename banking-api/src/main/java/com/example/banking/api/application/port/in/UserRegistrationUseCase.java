package com.example.banking.api.application.port.in;

import com.example.banking.api.domain.model.User;

/**
 * Inbound port for user registration use case.
 * Defines the contract for registering new users.
 */
public interface UserRegistrationUseCase {
    
    /**
     * Registers a new user in the system.
     * 
     * @param username The username for the new user
     * @param password The password for the new user
     * @return true if registration was successful, false otherwise
     * @throws IllegalArgumentException if username or password is invalid
     * @throws UserAlreadyExistsException if username already exists
     */
    boolean registerUser(String username, String password);
    
    /**
     * Exception thrown when attempting to register a user that already exists.
     */
    class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String username) {
            super("User already exists: " + username);
        }
    }
}
