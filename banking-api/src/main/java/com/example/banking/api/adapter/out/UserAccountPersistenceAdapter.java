package com.example.banking.api.adapter.out;

import com.example.banking.api.application.port.out.UserAccountPort;
import com.example.banking.api.domain.BankingTransaction;
import com.example.banking.api.domain.BankingUser;
import com.example.banking.api.service.BankingProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserAccountPersistenceAdapter implements UserAccountPort {

    private final BankingProcessService bankingProcessService;

    @Autowired
    public UserAccountPersistenceAdapter(BankingProcessService bankingProcessService) {
        this.bankingProcessService = bankingProcessService;
    }

    @Override
    public boolean registerUser(String username, String password) {
        return bankingProcessService.registerUser(username, password);
    }

    @Override
    public BankingUser authenticateUser(String username, String password) {
        return bankingProcessService.authenticateUser(username, password);
    }

    @Override
    public boolean deposit(String username, String password, double amount) {
        return bankingProcessService.deposit(username, password, amount);
    }

    @Override
    public boolean withdraw(String username, String password, double amount) {
        return bankingProcessService.withdraw(username, password, amount);
    }

    @Override
    public Double getBalance(String username, String password) {
        return bankingProcessService.getBalance(username, password);
    }

    @Override
    public List<BankingTransaction> getTransactions(String username, String password) {
        return bankingProcessService.getTransactions(username, password);
    }

    @Override
    public boolean deleteUser(String username, String password) {
        return bankingProcessService.deleteUser(username, password);
    }
}