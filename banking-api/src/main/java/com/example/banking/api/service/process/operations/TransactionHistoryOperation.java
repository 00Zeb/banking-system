package com.example.banking.api.service.process.operations;

import com.example.banking.api.model.BankingTransaction;
import com.example.banking.api.service.process.ProcessCommunication;
import com.example.banking.api.service.process.ProcessOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Process operation for getting transaction history.
 */
public class TransactionHistoryOperation implements ProcessOperation<List<BankingTransaction>> {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionHistoryOperation.class);
    
    private final String username;
    private final String password;
    
    // Pattern for parsing transactions
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile("\\[([0-9-: ]+)\\] ([^:]+): \\$([0-9]+\\.?[0-9]*)");
    private static final DateTimeFormatter TRANSACTION_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public TransactionHistoryOperation(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    @Override
    public List<BankingTransaction> execute(ProcessCommunication communication) throws Exception {
        logger.info("=== TRANSACTION HISTORY OPERATION START ===");
        
        // Wait for initial menu
        String initialOutput = communication.waitForInitialMenu();
        if (!initialOutput.toLowerCase().contains("login") && !initialOutput.toLowerCase().contains("choose")) {
            logger.error("No menu found in initial output");
            return null;
        }
        
        // Perform authentication
        String authResult = communication.authenticateUser(username, password);
        boolean isAuthenticated = communication.isAuthenticationSuccessful(authResult, username);
        
        if (!isAuthenticated) {
            logger.info("Authentication failed, exiting...");
            communication.performGracefulExit();
            return null;
        }
        
        // Wait for banking menu and list transactions
        logger.info("Waiting for banking menu...");
        String bankingMenuOutput = communication.readOutput(300);
        logger.info("Banking menu output: [{}]", bankingMenuOutput);
        
        // Send list transactions option
        logger.info("Sending list transactions option (3)...");
        communication.sendCommand("3"); // Choose list transactions option
        
        // Wait for transaction output
        logger.info("Waiting for transaction output...");
        String transactionOutput = communication.readOutput(500);
        logger.info("Transaction output: [{}]", transactionOutput);
        
        // Parse transactions from output
        List<BankingTransaction> transactions = parseTransactions(transactionOutput);
        logger.info("Parsed {} transactions", transactions.size());
        
        // Logout gracefully
        communication.performGracefulLogout();
        
        logger.info("=== TRANSACTION HISTORY OPERATION END - COUNT: {} ===", transactions.size());
        return transactions;
    }
    
    /**
     * Parse transactions from process output.
     */
    private List<BankingTransaction> parseTransactions(String output) {
        List<BankingTransaction> transactions = new ArrayList<>();
        Matcher matcher = TRANSACTION_PATTERN.matcher(output);
        
        while (matcher.find()) {
            try {
                String dateStr = matcher.group(1);
                String type = matcher.group(2);
                double amount = Double.parseDouble(matcher.group(3));
                
                LocalDateTime timestamp = LocalDateTime.parse(dateStr, TRANSACTION_DATE_FORMAT);
                transactions.add(new BankingTransaction(type, amount, timestamp));
                
            } catch (Exception e) {
                logger.warn("Failed to parse transaction: {}", matcher.group(0), e);
            }
        }
        
        return transactions;
    }
}