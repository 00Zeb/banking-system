package com.example.banking;

import org.mindrot.jbcrypt.BCrypt;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages user registration and login.
 * Acts as an in-memory database for users for this simple application.
 */
public class UserService {
    private final Map<String, User> users = new HashMap<>();

    /**
     * Registers a new user.
     * @param username The desired username.
     * @param password The desired password.
     * @return true if registration is successful, false if the username already exists.
     */
    public boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            System.out.println("Username already exists. Please choose another one.");
            return false;
        }
        // Hash the password for secure storage
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(username, hashedPassword);
        users.put(username, newUser);
        System.out.println("Registration successful!");
        return true;
    }

    /**
     * Logs in a user.
     * @param username The user's username.
     * @param password The user's password.
     * @return The User object if login is successful, otherwise null.
     */
    public User loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && BCrypt.checkpw(password, user.getHashedPassword())) {
            System.out.println("Login successful!");
            return user;
        }
        System.out.println("Invalid username or password.");
        return null;
    }
}
