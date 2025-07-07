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
    
    // Pattern for parsing balance
    private static final Pattern BALANCE_PATTERN = Pattern.compile("\\$([0-9]+\\.?[0-9]*)");
    
    public SessionBalanceOperation(String username) {
        this.username = username;
    }
    
    @Override
    public Double execute(ProcessCommunication communication) throws Exception {
        logger.info("=== SESSION BALANCE OPERATION START ===");
        
        // For session-based operations, we assume the process is already authenticated
        // Just send the balance check command directly
        logger.info("Sending balance check command (2)...");
        communication.sendCommand("2"); // Choose balance option from banking menu
        
        // Wait for balance output
        logger.info("Waiting for balance output...");
        String balanceOutput = communication.readOutput(500);
        logger.info("Balance output: [{}]", balanceOutput);
        
        // Parse balance from output
        Double balance = parseBalance(balanceOutput);
        logger.info("Parsed balance: {}", balance);
        
        logger.info("=== SESSION BALANCE OPERATION END - BALANCE: {} ===", balance);
        return balance;
    }
    
    /**
     * Parse balance from process output.
     */
    private Double parseBalance(String output) {
        // Look for patterns like "Current balance: $100.00" or "Balance: $50.25"
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