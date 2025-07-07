package com.example.banking.api.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for session-based login operations.
 * Contains user information and session details.
 */
public class SessionResponse {
    
    private String username;
    private double balance;
    private String sessionId;
    private LocalDateTime sessionCreatedAt;
    private LocalDateTime sessionExpiresAt;

    public SessionResponse() {}

    public SessionResponse(String username, double balance, String sessionId, 
                          LocalDateTime sessionCreatedAt, LocalDateTime sessionExpiresAt) {
        this.username = username;
        this.balance = balance;
        this.sessionId = sessionId;
        this.sessionCreatedAt = sessionCreatedAt;
        this.sessionExpiresAt = sessionExpiresAt;
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getSessionCreatedAt() {
        return sessionCreatedAt;
    }

    public void setSessionCreatedAt(LocalDateTime sessionCreatedAt) {
        this.sessionCreatedAt = sessionCreatedAt;
    }

    public LocalDateTime getSessionExpiresAt() {
        return sessionExpiresAt;
    }

    public void setSessionExpiresAt(LocalDateTime sessionExpiresAt) {
        this.sessionExpiresAt = sessionExpiresAt;
    }
}