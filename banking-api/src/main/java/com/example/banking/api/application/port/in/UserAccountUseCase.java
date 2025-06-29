package com.example.banking.api.application.port.in;

import com.example.banking.api.domain.BankingTransaction;
import com.example.banking.api.domain.BankingUser;

import java.util.List;

public interface UserAccountUseCase {
    boolean registerUser(String username, String password);
    BankingUser authenticateUser(String username, String password);
    boolean deposit(String username, String password, double amount);
    boolean withdraw(String username, String password, double amount);
    Double getBalance(String username, String password);
    List<BankingTransaction> getTransactions(String username, String password);
    boolean deleteUser(String username, String password);
}