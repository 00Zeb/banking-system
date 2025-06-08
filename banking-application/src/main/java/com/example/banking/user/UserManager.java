package com.example.banking.user;

import com.example.banking.persistence.UserRepository;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages users in the banking system.
 */
public class UserManager {
    private Map<String, User> users;
    private UserRepository repository;

    public UserManager() {
        this.users = new HashMap<>();
        // Add some default users for testing
        registerUser("admin", "admin123");
        registerUser("john", "pass123");
    }
    
    // Constructor with dependency injection for repository
    public UserManager(UserRepository repository) {
        this.users = new HashMap<>();
        this.repository = repository;
        
        // Load users from repository if available
        if (repository != null) {
            repository.getAllUsers().forEach(user -> {
                user.setUserManager(this); // Set the user manager reference
                user.getAccount().setOwner(user); // Set the account owner reference
                users.put(user.getUsername(), user);
            });
        } else {
            // Add default users if no repository
            registerUser("admin", "admin123");
            registerUser("john", "pass123");
        }
    }

    public boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        
        User newUser = new User(username, password);
        newUser.setUserManager(this); // Set the user manager reference
        users.put(username, newUser);
        
        // Save to repository if available
        if (repository != null) {
            repository.saveUser(newUser);
        }
        
        return true;
    }

    public User authenticateUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.authenticate(password)) {
            return user;
        }
        return null;
    }
    
    public boolean deleteUser(String username) {
        if (!users.containsKey(username)) {
            return false;
        }
        
        users.remove(username);
        
        // Delete from repository if available
        if (repository != null) {
            repository.deleteUser(username);
        }
        
        return true;
    }
    
    /**
     * Updates a user in the repository.
     * This is called after transactions to ensure they are persisted.
     */
    public void updateUser(User user) {
        if (repository != null && users.containsKey(user.getUsername())) {
            repository.updateUser(user);
        }
    }
    
    /**
     * Saves all users to the repository.
     * This can be called when the application exits.
     */
    public void saveAllUsers() {
        if (repository != null) {
            repository.saveAllUsers();
        }
    }
}