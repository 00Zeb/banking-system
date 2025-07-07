package com.example.banking.api.service.process.operations;

import com.example.banking.api.service.process.ProcessCommunication;
import com.example.banking.api.service.process.ProcessOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session-aware deposit operation that works with pre-authenticated processes.
 * This operation assumes the process is already authenticated and skips authentication.
 */
public class SessionDepositOperation implements ProcessOperation<Boolean> {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionDepositOperation.class);
    
    private final String username;
    private final double amount;
    
    public SessionDepositOperation(String username, double amount) {
        this.username = username;
        this.amount = amount;
    }
    
    @Override
    public Boolean execute(ProcessCommunication communication) throws Exception {
        logger.info("=== SESSION DEPOSIT OPERATION START - Amount: {} ===", amount);
        
        // For session-based operations, we assume the process is already authenticated
        // Send the deposit command directly
        logger.info("Sending deposit command (3)...");
        communication.sendCommand("3"); // Choose deposit option from banking menu
        
        // Wait for amount prompt
        logger.info("Waiting for amount prompt...");
        String amountPrompt = communication.readOutput(500);
        logger.info("Amount prompt: [{}]", amountPrompt);
        
        // Send the amount
        logger.info("Sending amount: {}", amount);
        communication.sendCommand(String.valueOf(amount));
        
        // Wait for deposit result
        logger.info("Waiting for deposit result...");
        String depositResult = communication.readOutput(1000);
        logger.info("Deposit result: [{}]", depositResult);
        
        // Check if deposit was successful
        boolean success = depositResult.toLowerCase().contains("successful") || 
                         depositResult.toLowerCase().contains("deposited");
        
        logger.info("=== SESSION DEPOSIT OPERATION END - Success: {} ===", success);
        return success;
    }
}