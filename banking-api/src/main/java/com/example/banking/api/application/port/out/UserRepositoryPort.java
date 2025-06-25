package com.example.banking.api.application.port.out;

import com.example.banking.api.domain.model.Account;
import com.example.banking.api.domain.model.User;

import java.util.Optional;

/**
 * Outbound port for user repository operations.
 * Defines the contract for persisting and retrieving user data.
 */
public interface UserRepositoryPort {
    
    /**
     * Saves a user to the repository.
     * 
     * @param user The user to save
     * @return true if save was successful, false otherwise
     */
    boolean saveUser(User user);
    
    /**
     * Finds a user by username.
     * 
     * @param username The username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findUserByUsername(String username);
    
    /**
     * Authenticates a user with username and password.
     * 
     * @param username The username
     * @param password The password
     * @return Optional containing the user if authentication successful, empty otherwise
     */
    Optional<User> authenticateUser(String username, String password);
    
    /**
     * Deletes a user from the repository.
     * 
     * @param username The username of the user to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteUser(String username);
    
    /**
     * Checks if a user exists in the repository.
     * 
     * @param username The username to check
     * @return true if user exists, false otherwise
     */
    boolean userExists(String username);
}
