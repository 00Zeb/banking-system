package com.example.banking.api.service.process.operations;

import com.example.banking.api.service.process.ProcessCommunication;
import com.example.banking.api.service.process.ProcessOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process operation for user registration.
 */
public class UserRegistrationOperation implements ProcessOperation<Boolean> {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationOperation.class);
    
    private final String username;
    private final String password;
    
    public UserRegistrationOperation(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    @Override
    public Boolean execute(ProcessCommunication communication) throws Exception {
        logger.info("=== USER REGISTRATION OPERATION START ===");
        
        // Wait for initial menu
        String initialOutput = communication.waitForInitialMenu();
        if (!initialOutput.toLowerCase().contains("register") && !initialOutput.toLowerCase().contains("choose")) {
            logger.error("No menu found in initial output");
            return false;
        }
        
        // Navigate to registration
        communication.sendCommand("2"); // Choose register option
        
        // Enter username
        communication.sendCommand(username);
        
        // Enter password
        communication.sendCommand(password);
        
        // Exit application
        communication.sendCommand("3"); // Exit from main menu
        
        // Read output to determine success
        String output = communication.readCleanOutput();
        logger.debug("Registration output: {}", output);
        
        boolean success = output.contains("Registration successful!");
        
        logger.info("=== USER REGISTRATION OPERATION END - SUCCESS: {} ===", success);
        return success;
    }
}