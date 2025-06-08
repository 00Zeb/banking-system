package com.example.banking.persistence;

import com.example.banking.User;
import java.util.List;

/**
 * Interface for user data persistence.
 */
public interface UserRepository {
    /**
     * Saves a user to the repository.
     */
    void saveUser(User user);
    
    /**
     * Retrieves a user by username.
     */
    User getUserByUsername(String username);
    
    /**
     * Lists all users in the repository.
     */
    List<User> getAllUsers();
    
    /**
     * Deletes a user from the repository.
     */
    boolean deleteUser(String username);
}