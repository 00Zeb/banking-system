package com.example.banking.api.service.process.operations;

import com.example.banking.api.model.BankingUser;
import com.example.banking.api.service.process.ProcessCommunication;
import com.example.banking.api.service.process.ProcessOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process operation for user authentication.
 */
public class UserAuthenticationOperation implements ProcessOperation<BankingUser> {
    
    private static final Logger logger = LoggerFactory.getLogger(UserAuthenticationOperation.class);
    
    private final String username;
    private final String password;
    
    public UserAuthenticationOperation(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    @Override
    public BankingUser execute(ProcessCommunication communication) throws Exception {
        logger.info("=== USER AUTHENTICATION OPERATION START ===");
        
        // Wait for initial menu
        String initialOutput = communication.waitForInitialMenu();
        if (!initialOutput.toLowerCase().contains("login") && !initialOutput.toLowerCase().contains("choose")) {
            logger.error("No menu found in initial output");
            return null;
        }
        
        // Perform authentication
        String authResult = communication.authenticateUser(username, password);
        
        // Analyze the result
        boolean isSuccessful = communication.isAuthenticationSuccessful(authResult, username);
        logger.info("Authentication successful: {}", isSuccessful);
        
        if (isSuccessful) {
            // Perform graceful logout
            communication.performGracefulLogout();
            
            logger.info("=== USER AUTHENTICATION OPERATION END - SUCCESS ===");
            return new BankingUser(username, 0.0);
        } else {
            // Handle failed login
            communication.performGracefulExit();
            
            logger.info("=== USER AUTHENTICATION OPERATION END - FAILURE ===");
            return null;
        }
    }
}