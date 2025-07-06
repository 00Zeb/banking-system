package com.example.banking.api.service.process.operations;

import com.example.banking.api.service.process.ProcessCommunication;
import com.example.banking.api.service.process.ProcessOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Process operation for getting account balance.
 */
public class BalanceOperation implements ProcessOperation<Double> {
    
    private static final Logger logger = LoggerFactory.getLogger(BalanceOperation.class);
    
    private final String username;
    private final String password;
    
    // Pattern for parsing balance
    private static final Pattern BALANCE_PATTERN = Pattern.compile("Current Balance: \\$([0-9]+\\.?[0-9]*)");
    
    public BalanceOperation(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    @Override
    public Double execute(ProcessCommunication communication) throws Exception {
        logger.info("=== BALANCE OPERATION START ===");
        
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
        
        // Parse balance from output
        double balance = parseBalance(transactionOutput);
        logger.info("Parsed balance: {}", balance);
        
        // Logout gracefully
        communication.performGracefulLogout();
        
        logger.info("=== BALANCE OPERATION END - BALANCE: {} ===", balance);
        return balance;
    }
    
    /**
     * Parse balance from process output.
     */
    private double parseBalance(String output) {
        Matcher matcher = BALANCE_PATTERN.matcher(output);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse balance from output: {}", matcher.group(1));
            }
        }
        return 0.0;
    }
}