package com.example.banking;

/**
 * Represents a user of the banking application.
 * Stores the username and the securely hashed password.
 */
public class User {
    private String username;
    private String hashedPassword;

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }
}
