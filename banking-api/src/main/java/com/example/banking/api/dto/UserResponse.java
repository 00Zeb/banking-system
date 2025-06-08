package com.example.banking.api.dto;

/**
 * Response DTO for user information.
 */
public class UserResponse {
    
    private String username;
    private double balance;

    public UserResponse() {}

    public UserResponse(String username, double balance) {
        this.username = username;
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
