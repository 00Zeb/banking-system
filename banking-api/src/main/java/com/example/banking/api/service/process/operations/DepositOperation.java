package com.example.banking.api.service.process.operations;

import com.example.banking.api.service.process.ProcessCommunication;
import com.example.banking.api.service.process.ProcessOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process operation for depositing money.
 */
public class DepositOperation implements ProcessOperation<Boolean> {
    
    private static final Logger logger = LoggerFactory.getLogger(DepositOperation.class);
    
    private final String username;
    private final String password;
    private final double amount;
    
    public DepositOperation(String username, String password, double amount) {
        this.username = username;
        this.password = password;
        this.amount = amount;
    }
    
    @Override
    public Boolean execute(ProcessCommunication communication) throws Exception {
        logger.info("=== DEPOSIT OPERATION START ===");
        
        // Wait for initial menu
        String initialOutput = communication.waitForInitialMenu();
        if (!initialOutput.toLowerCase().contains("login") && !initialOutput.toLowerCase().contains("choose")) {
            logger.error("No menu found in initial output");
            return false;
        }
        
        // Perform authentication
        String authResult = communication.authenticateUser(username, password);
        boolean isAuthenticated = communication.isAuthenticationSuccessful(authResult, username);
        
        if (!isAuthenticated) {
            logger.info("Authentication failed, exiting...");
            communication.performGracefulExit();
            return false;
        }
        
        // Wait for banking menu and perform deposit
        logger.info("Waiting for banking menu...");
        String bankingMenuOutput = communication.readOutput(300);
        logger.info("Banking menu output: [{}]", bankingMenuOutput);
        
        // Send deposit option
        logger.info("Sending deposit option (1)...");
        communication.sendCommand("1"); // Choose deposit option
        
        // Wait for deposit amount prompt
        logger.info("Waiting for deposit amount prompt...");
        String depositPromptOutput = communication.readOutput(300);
        logger.info("Deposit prompt output: [{}]", depositPromptOutput);
        
        // Send deposit amount
        logger.info("Sending deposit amount: {}", amount);
        communication.sendCommand(String.valueOf(amount));
        
        // Wait for deposit result
        logger.info("Waiting for deposit result...");
        String depositResult = communication.readOutput(500);
        logger.info("Deposit result: [{}]", depositResult);
        
        // Check if deposit was successful
        boolean depositSuccess = depositResult.toLowerCase().contains("successfully deposited") ||
                               depositResult.toLowerCase().contains("deposit successful") ||
                               depositResult.contains("Successfully deposited $" + amount);
        logger.info("Deposit successful: {}", depositSuccess);
        
        // Logout gracefully
        communication.performGracefulLogout();
        
        logger.info("=== DEPOSIT OPERATION END - SUCCESS: {} ===", depositSuccess);
        return depositSuccess;
    }
}