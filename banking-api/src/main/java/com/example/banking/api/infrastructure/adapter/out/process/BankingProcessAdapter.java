package com.example.banking.api.infrastructure.adapter.out.process;

import com.example.banking.api.application.port.out.BankingSystemPort;
import com.example.banking.api.domain.model.Account;
import com.example.banking.api.domain.model.Money;
import com.example.banking.api.domain.model.Transaction;
import com.example.banking.api.domain.model.User;
import com.example.banking.api.model.BankingTransaction;
import com.example.banking.api.model.BankingUser;
import com.example.banking.api.service.BankingProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Infrastructure adapter that implements BankingSystemPort by delegating to BankingProcessService.
 * This adapter converts between domain objects and the legacy process service.
 */
@Component
public class BankingProcessAdapter implements BankingSystemPort {
    
    private static final Logger logger = LoggerFactory.getLogger(BankingProcessAdapter.class);
    
    private final BankingProcessService processService;
    
    public BankingProcessAdapter(BankingProcessService processService) {
        this.processService = processService;
    }
    
    @Override
    public boolean registerUser(User user) {
        logger.debug("Registering user: {}", user.getUsername());
        return processService.registerUser(user.getUsername(), user.getPassword());
    }
    
    @Override
    public Optional<Account> authenticateUser(String username, String password) {
        logger.debug("Authenticating user: {}", username);
        
        BankingUser bankingUser = processService.authenticateUser(username, password);
        if (bankingUser == null) {
            return Optional.empty();
        }
        
        // Convert BankingUser to Account
        Account account = convertToAccount(bankingUser);
        return Optional.of(account);
    }
    
    @Override
    public boolean deposit(String username, String password, Money amount) {
        logger.debug("Depositing {} for user: {}", amount, username);
        return processService.deposit(username, password, amount.toDouble());
    }
    
    @Override
    public boolean withdraw(String username, String password, Money amount) {
        logger.debug("Withdrawing {} for user: {}", amount, username);
        return processService.withdraw(username, password, amount.toDouble());
    }
    
    @Override
    public Optional<Money> getBalance(String username, String password) {
        logger.debug("Getting balance for user: {}", username);
        
        Double balance = processService.getBalance(username, password);
        if (balance == null) {
            return Optional.empty();
        }
        
        return Optional.of(new Money(balance));
    }
    
    @Override
    public Optional<List<Transaction>> getTransactionHistory(String username, String password) {
        logger.debug("Getting transaction history for user: {}", username);
        
        List<BankingTransaction> bankingTransactions = processService.getTransactions(username, password);
        if (bankingTransactions == null) {
            return Optional.empty();
        }
        
        // Convert BankingTransaction list to Transaction list
        List<Transaction> transactions = bankingTransactions.stream()
                .map(this::convertToTransaction)
                .collect(Collectors.toList());
        
        return Optional.of(transactions);
    }
    
    @Override
    public boolean deleteUser(String username, String password) {
        logger.debug("Deleting user: {}", username);
        return processService.deleteUser(username, password);
    }
    
    /**
     * Converts a BankingUser to an Account domain object.
     */
    private Account convertToAccount(BankingUser bankingUser) {
        Account account = new Account(bankingUser.getUsername(), new Money(bankingUser.getBalance()));
        
        // Convert and add transactions to the account
        if (bankingUser.getTransactions() != null) {
            for (BankingTransaction bankingTransaction : bankingUser.getTransactions()) {
                Transaction transaction = convertToTransaction(bankingTransaction);
                // Note: We can't directly add transactions to the account since they're created through operations
                // In a real implementation, we might need to reconstruct the account state differently
            }
        }
        
        return account;
    }
    
    /**
     * Converts a BankingTransaction to a Transaction domain object.
     */
    private Transaction convertToTransaction(BankingTransaction bankingTransaction) {
        Money amount = new Money(bankingTransaction.getAmount());
        LocalDateTime timestamp = bankingTransaction.getTimestamp();
        
        // Determine transaction type based on the type string
        Transaction.Type type;
        if (bankingTransaction.getType().toLowerCase().contains("deposit")) {
            type = Transaction.Type.DEPOSIT;
        } else if (bankingTransaction.getType().toLowerCase().contains("withdraw")) {
            type = Transaction.Type.WITHDRAWAL;
        } else {
            // Default to deposit if we can't determine the type
            type = Transaction.Type.DEPOSIT;
        }
        
        return new Transaction(type, amount, timestamp, bankingTransaction.getType());
    }
}
