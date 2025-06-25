package com.example.banking.api.application.service;

import com.example.banking.api.application.port.in.AccountManagementUseCase;
import com.example.banking.api.application.port.out.BankingSystemPort;
import org.springframework.stereotype.Service;

/**
 * Application service implementing account management use cases.
 * Handles account-related operations like deletion.
 */
@Service
public class AccountManagementService implements AccountManagementUseCase {
    
    private final BankingSystemPort bankingSystemPort;
    
    public AccountManagementService(BankingSystemPort bankingSystemPort) {
        this.bankingSystemPort = bankingSystemPort;
    }
    
    @Override
    public boolean deleteAccount(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        return bankingSystemPort.deleteUser(username.trim(), password);
    }
}
