package com.example.banking.api.application.service;

import com.example.banking.api.application.port.in.UserAccountUseCase;
import com.example.banking.api.application.port.out.UserAccountPort;
import com.example.banking.api.domain.BankingTransaction;
import com.example.banking.api.domain.BankingUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAccountService implements UserAccountUseCase {

    private final UserAccountPort userAccountPort;

    @Autowired
    public UserAccountService(UserAccountPort userAccountPort) {
        this.userAccountPort = userAccountPort;
    }

    @Override
    public boolean registerUser(String username, String password) {
        return userAccountPort.registerUser(username, password);
    }

    @Override
    public BankingUser authenticateUser(String username, String password) {
        return userAccountPort.authenticateUser(username, password);
    }

    @Override
    public boolean deposit(String username, String password, double amount) {
        return userAccountPort.deposit(username, password, amount);
    }

    @Override
    public boolean withdraw(String username, String password, double amount) {
        return userAccountPort.withdraw(username, password, amount);
    }

    @Override
    public Double getBalance(String username, String password) {
        return userAccountPort.getBalance(username, password);
    }

    @Override
    public List<BankingTransaction> getTransactions(String username, String password) {
        return userAccountPort.getTransactions(username, password);
    }

    @Override
    public boolean deleteUser(String username, String password) {
        return userAccountPort.deleteUser(username, password);
    }
}