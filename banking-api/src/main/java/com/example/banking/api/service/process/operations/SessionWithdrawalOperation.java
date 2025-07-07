package com.example.banking.api.service.process.operations;

import com.example.banking.api.service.process.ProcessCommunication;
import com.example.banking.api.service.process.ProcessOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session-aware withdrawal operation that works with pre-authenticated processes.
 * This operation assumes the process is already authenticated and skips authentication.
 */
public class SessionWithdrawalOperation implements ProcessOperation<Boolean> {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionWithdrawalOperation.class);
    
    private final String username;
    private final double amount;
    
    public SessionWithdrawalOperation(String username, double amount) {
        this.username = username;
        this.amount = amount;
    }
    
    @Override
    public Boolean execute(ProcessCommunication communication) throws Exception {
        logger.info("=== SESSION WITHDRAWAL OPERATION START - Amount: {} ===", amount);
        
        // For session-based operations, we assume the process is already authenticated
        // Send the withdrawal command directly
        logger.info("Sending withdrawal command (4)...");
        communication.sendCommand("4"); // Choose withdrawal option from banking menu
        
        // Wait for amount prompt
        logger.info("Waiting for amount prompt...");
        String amountPrompt = communication.readOutput(500);
        logger.info("Amount prompt: [{}]", amountPrompt);
        
        // Send the amount
        logger.info("Sending amount: {}", amount);
        communication.sendCommand(String.valueOf(amount));
        
        // Wait for withdrawal result
        logger.info("Waiting for withdrawal result...");
        String withdrawalResult = communication.readOutput(1000);
        logger.info("Withdrawal result: [{}]", withdrawalResult);
        
        // Check if withdrawal was successful
        boolean success = withdrawalResult.toLowerCase().contains("successful") || 
                         withdrawalResult.toLowerCase().contains("withdrawn");
        
        // Check for insufficient funds
        if (withdrawalResult.toLowerCase().contains("insufficient")) {
            logger.info("Withdrawal failed due to insufficient funds");
            success = false;
        }
        
        logger.info("=== SESSION WITHDRAWAL OPERATION END - Success: {} ===", success);
        return success;
    }
}