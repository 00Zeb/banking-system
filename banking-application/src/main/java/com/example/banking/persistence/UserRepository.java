package com.example.banking.persistence;

import com.example.banking.user.User;
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
     * Updates an existing user in the repository.
     */
    void updateUser(User user);
    
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
    
    /**
     * Saves all users in the cache.
     */
    void saveAllUsers();
}