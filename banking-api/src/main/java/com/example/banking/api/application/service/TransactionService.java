package com.example.banking.api.application.service;

import com.example.banking.api.application.port.in.TransactionUseCase;
import com.example.banking.api.application.port.out.BankingSystemPort;
import com.example.banking.api.domain.model.Account;
import com.example.banking.api.domain.model.Money;
import com.example.banking.api.domain.model.Transaction;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Application service implementing transaction use cases.
 * Orchestrates transaction operations through the external banking system.
 */
@Service
public class TransactionService implements TransactionUseCase {
    
    private final BankingSystemPort bankingSystemPort;
    
    public TransactionService(BankingSystemPort bankingSystemPort) {
        this.bankingSystemPort = bankingSystemPort;
    }
    
    @Override
    public Optional<Transaction> deposit(String username, String password, Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        boolean success = bankingSystemPort.deposit(username, password, amount);
        if (success) {
            return Optional.of(Transaction.deposit(amount, LocalDateTime.now()));
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Transaction> withdraw(String username, String password, Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        boolean success = bankingSystemPort.withdraw(username, password, amount);
        if (success) {
            return Optional.of(Transaction.withdrawal(amount, LocalDateTime.now()));
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Account> getAccount(String username, String password) {
        return bankingSystemPort.authenticateUser(username, password);
    }
    
    @Override
    public Optional<List<Transaction>> getTransactionHistory(String username, String password) {
        return bankingSystemPort.getTransactionHistory(username, password);
    }
}
