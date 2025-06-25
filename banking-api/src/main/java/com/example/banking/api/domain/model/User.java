package com.example.banking.api.domain.model;

import java.util.Objects;

/**
 * Domain entity representing a banking user.
 * This is a pure domain object with no dependencies on frameworks or infrastructure.
 */
public class User {
    
    private final String username;
    private final String password;
    
    public User(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        this.username = username.trim();
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }
}
