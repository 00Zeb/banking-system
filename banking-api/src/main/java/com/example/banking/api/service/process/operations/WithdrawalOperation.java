package com.example.banking.api.service.process.operations;

import com.example.banking.api.service.process.ProcessCommunication;
import com.example.banking.api.service.process.ProcessOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process operation for withdrawing money.
 */
public class WithdrawalOperation implements ProcessOperation<Boolean> {
    
    private static final Logger logger = LoggerFactory.getLogger(WithdrawalOperation.class);
    
    private final String username;
    private final String password;
    private final double amount;
    
    public WithdrawalOperation(String username, String password, double amount) {
        this.username = username;
        this.password = password;
        this.amount = amount;
    }
    
    @Override
    public Boolean execute(ProcessCommunication communication) throws Exception {
        logger.info("=== WITHDRAWAL OPERATION START ===");
        
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
        
        // Wait for banking menu and perform withdrawal
        logger.info("Waiting for banking menu...");
        String bankingMenuOutput = communication.readOutput(300);
        logger.info("Banking menu output: [{}]", bankingMenuOutput);
        
        // Send withdrawal option
        logger.info("Sending withdrawal option (2)...");
        communication.sendCommand("2"); // Choose withdraw option
        
        // Wait for withdrawal amount prompt
        logger.info("Waiting for withdrawal amount prompt...");
        String withdrawPromptOutput = communication.readOutput(300);
        logger.info("Withdrawal prompt output: [{}]", withdrawPromptOutput);
        
        // Send withdrawal amount
        logger.info("Sending withdrawal amount: {}", amount);
        communication.sendCommand(String.valueOf(amount));
        
        // Wait for withdrawal result
        logger.info("Waiting for withdrawal result...");
        String withdrawResult = communication.readOutput(500);
        logger.info("Withdrawal result: [{}]", withdrawResult);
        
        // Check if withdrawal was successful
        boolean withdrawSuccess = withdrawResult.toLowerCase().contains("successfully withdrew") ||
                                withdrawResult.toLowerCase().contains("withdrawal successful") ||
                                withdrawResult.contains("Successfully withdrew $" + amount);
        logger.info("Withdrawal successful: {}", withdrawSuccess);
        
        // Logout gracefully
        communication.performGracefulLogout();
        
        logger.info("=== WITHDRAWAL OPERATION END - SUCCESS: {} ===", withdrawSuccess);
        return withdrawSuccess;
    }
}