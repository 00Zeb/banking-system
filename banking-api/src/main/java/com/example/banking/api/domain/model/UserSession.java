package com.example.banking.api.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserSession {
    
    private final String sessionId;
    private final String username;
    private final LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private boolean active;
    private Process bankingProcess;
    private String processId;
    
    public UserSession(String sessionId, String username) {
        this.sessionId = sessionId;
        this.username = username;
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
        this.active = true;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    public void updateLastAccessed() {
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Process getBankingProcess() {
        return bankingProcess;
    }
    
    public void setBankingProcess(Process bankingProcess) {
        this.bankingProcess = bankingProcess;
    }
    
    public String getProcessId() {
        return processId;
    }
    
    public void setProcessId(String processId) {
        this.processId = processId;
    }
    
    public boolean hasActiveProcess() {
        return bankingProcess != null && bankingProcess.isAlive();
    }
    
    public boolean isExpired(long timeoutInSeconds) {
        return lastAccessedAt.plusSeconds(timeoutInSeconds).isBefore(LocalDateTime.now());
    }
    
    public long getSessionDurationInSeconds() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).getSeconds();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }
    
    @Override
    public String toString() {
        return "UserSession{" +
                "sessionId='" + sessionId + '\'' +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", lastAccessedAt=" + lastAccessedAt +
                ", active=" + active +
                ", hasActiveProcess=" + hasActiveProcess() +
                '}';
    }
}