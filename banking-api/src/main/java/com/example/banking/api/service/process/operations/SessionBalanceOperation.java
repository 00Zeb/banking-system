package com.example.banking.api.service.process.operations;

import com.example.banking.api.service.process.ProcessCommunication;
import com.example.banking.api.service.process.ProcessOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Session-aware balance operation that works with pre-authenticated processes.
 * This operation assumes the process is already authenticated and skips authentication.
 */
public class SessionBalanceOperation implements ProcessOperation<Double> {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionBalanceOperation.class);
    
    private final String username;
    
    // Pattern for parsing balance - specifically looks for "Current Balance: $amount"
    private static final Pattern BALANCE_PATTERN = Pattern.compile("Current Balance:\\s*\\$([0-9]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE);
    
    public SessionBalanceOperation(String username) {
        this.username = username;
    }
    
    @Override
    public Double execute(ProcessCommunication communication) throws Exception {
        logger.info("=== SESSION BALANCE OPERATION START ===");
        
        // For session-based operations, we assume the process is already authenticated
        // and in the banking menu state. Use transaction list to get balance
        logger.info("Sending transaction list command (3)...");
        communication.sendCommand("3"); // Choose transaction list option from banking menu
        
        // Wait for transaction list output (which includes current balance)
        logger.info("Waiting for transaction list output...");
        String transactionOutput = communication.readOutput(500);
        logger.info("Transaction list output: [{}]", transactionOutput);
        
        // Parse balance from transaction list output
        Double balance = parseBalance(transactionOutput);
        logger.info("Parsed balance: {}", balance);
        
        logger.info("=== SESSION BALANCE OPERATION END - BALANCE: {} ===", balance);
        return balance;
    }
    
    /**
     * Parse balance from process output.
     * Looks for patterns like "Current Balance: $100.00" or "Current balance: $50.25"
     */
    private Double parseBalance(String output) {
        if (output == null || output.trim().isEmpty()) {
            logger.warn("Empty output provided for balance parsing");
            return 0.0;
        }

        // Look for patterns like "Current Balance: $100.00" or "Current balance: $50.25"
        Matcher matcher = BALANCE_PATTERN.matcher(output);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse balance from: {}", matcher.group(1), e);
            }
        }

        // Fallback: look for any number that might be a balance
        logger.warn("Could not parse balance from output: {}", output);
        return 0.0; // Default to 0 if we can't parse
    }
}