package com.example.banking.api.application.port.in;

/**
 * Inbound port for account management use cases.
 * Defines contracts for account-related operations like deletion.
 */
public interface AccountManagementUseCase {
    
    /**
     * Deletes a user account from the system.
     * 
     * @param username The username
     * @param password The password for authentication
     * @return true if deletion was successful, false if authentication failed
     */
    boolean deleteAccount(String username, String password);
}
