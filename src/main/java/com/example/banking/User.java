package com.example.banking;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Represents a user in the banking system with basic security.
 * Implements Serializable for persistence.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String passwordHash;
    private Account account;
    private transient UserManager userManager; // Not serialized

    /**
     * Creates a new user with the given username and password.
     */
    public User(String username, String password) {
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.account = new Account();
        this.account.setOwner(this); // Set the owner reference
    }
    
    /**
     * Sets the user manager for this user.
     * This is used for persistence updates.
     */
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }
    
    /**
     * Gets the user manager for this user.
     */
    public UserManager getUserManager() {
        return userManager;
    }

    /**
     * Verifies if the provided password matches the stored hash.
     */
    public boolean authenticate(String password) {
        String hashedInput = hashPassword(password);
        return this.passwordHash.equals(hashedInput);
    }

    /**
     * Simple password hashing using SHA-256.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            // If SHA-256 is not available, use a simple fallback (not recommended for production)
            return password + "_hashed";
        }
    }

    /**
     * Gets the username of this user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the account associated with this user.
     */
    public Account getAccount() {
        return account;
    }
    
    /**
     * Gets the password hash for storage purposes.
     */
    public String getPasswordHash() {
        return passwordHash;
    }
}