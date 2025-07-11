package com.example.banking.api.service;

import com.example.banking.api.domain.model.UserSession;
import com.example.banking.api.model.BankingTransaction;
import com.example.banking.api.model.BankingUser;
import com.example.banking.api.service.process.ProcessSessionManager;
import com.example.banking.api.service.process.operations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Session-based banking service that uses persistent processes for operations.
 * This service uses the ProcessSessionManager to execute operations on
 * session-specific persistent processes.
 */
@Service
public class SessionBankingService {

    private final ProcessSessionManager processSessionManager;

    @Autowired
    public SessionBankingService(ProcessSessionManager processSessionManager) {
        this.processSessionManager = processSessionManager;
    }

    /**
     * Get account balance for a session-authenticated user.
     */
    public Double getBalance(UserSession userSession) {
        try {
            SessionBalanceOperation operation = new SessionBalanceOperation(userSession.getUsername());
            return processSessionManager.executeForSession(userSession, operation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get balance for session", e);
        }
    }

    /**
     * Perform a deposit operation for a session-authenticated user.
     */
    public boolean deposit(UserSession userSession, double amount) {
        if (amount <= 0) {
            return false;
        }
        try {
            SessionDepositOperation operation = new SessionDepositOperation(userSession.getUsername(), amount);
            return processSessionManager.executeForSession(userSession, operation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform deposit for session", e);
        }
    }

    /**
     * Perform a withdrawal operation for a session-authenticated user.
     */
    public boolean withdraw(UserSession userSession, double amount) {
        if (amount <= 0) {
            return false;
        }
        try {
            SessionWithdrawalOperation operation = new SessionWithdrawalOperation(userSession.getUsername(), amount);
            return processSessionManager.executeForSession(userSession, operation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform withdrawal for session", e);
        }
    }

    /**
     * Get transaction history for a session-authenticated user.
     */
    public List<BankingTransaction> getTransactions(UserSession userSession) {
        try {
            SessionTransactionHistoryOperation operation = new SessionTransactionHistoryOperation(userSession.getUsername());
            return processSessionManager.executeForSession(userSession, operation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get transactions for session", e);
        }
    }

    /**
     * Delete a user account for a session-authenticated user.
     * TODO: Implement UserDeletionOperation when available.
     */
    public boolean deleteUser(UserSession userSession) {
        throw new UnsupportedOperationException("User deletion not yet implemented for session-based operations");
    }

    /**
     * Authenticate and establish session for a user.
     * This creates an authenticated process that stays in the banking menu state
     * for subsequent session operations to use.
     */
    public BankingUser authenticateAndStartSession(UserSession userSession, String password) {
        try {
            SessionAuthenticationOperation operation = new SessionAuthenticationOperation(userSession.getUsername(), password);
            return processSessionManager.authenticateForSession(userSession, operation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to authenticate and start session", e);
        }
    }
}