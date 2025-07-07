package com.example.banking.api.service;

import com.example.banking.api.config.BankingApplicationProperties;
import com.example.banking.api.service.process.ProcessExecutor;
import com.example.banking.api.model.BankingUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.File;

/**
 * Diagnostic test to understand login communication issues.
 */
@EnabledIfSystemProperty(named = "integration.tests", matches = "true")
class LoginDiagnosticTest {

    private BankingProcessService processService;
    private BankingApplicationProperties properties;
    private JarLocatorService jarLocatorService;

    @BeforeEach
    void setUp() {
        properties = new BankingApplicationProperties();
        properties.setProcessTimeout(15000); // 15 seconds for diagnostic

        // Initialize JAR locator service
        jarLocatorService = new JarLocatorService();
        jarLocatorService.init();

        // Check if JAR is accessible
        if (!jarLocatorService.isJarAccessible()) {
            throw new RuntimeException("Banking application JAR not accessible: " +
                jarLocatorService.getJarInfo() + ". Please build the banking-application first.");
        }

        System.out.println("Using JAR: " + jarLocatorService.getJarInfo());

        ProcessExecutor processExecutor = new ProcessExecutor(properties, jarLocatorService);
        processService = new BankingProcessService(processExecutor);

        // Clean up any existing data files
        cleanupDataFiles();
    }

    private void cleanupDataFiles() {
        File dataFile = new File("banking_data.ser");
        if (dataFile.exists()) {
            dataFile.delete();
        }
    }

    @Test
    void diagnoseLoginFlow() {
        System.out.println("\n=== DIAGNOSTIC TEST START ===");
        
        // First, try to register a user
        System.out.println("\n--- Registering test user ---");
        boolean registered = processService.registerUser("testuser", "password123");
        System.out.println("Registration result: " + registered);
        
        // Now try to authenticate
        System.out.println("\n--- Attempting authentication ---");
        BankingUser user = processService.authenticateUser("testuser", "password123");
        System.out.println("Authentication result: " + (user != null ? "SUCCESS" : "FAILURE"));
        
        if (user != null) {
            System.out.println("User: " + user.getUsername() + ", Balance: " + user.getBalance());
        }
        
        System.out.println("\n=== DIAGNOSTIC TEST END ===");
    }
    
    @Test
    void diagnoseLoginWithWrongPassword() {
        System.out.println("\n=== DIAGNOSTIC TEST - WRONG PASSWORD ===");
        
        // First, register a user
        System.out.println("\n--- Registering test user ---");
        boolean registered = processService.registerUser("testuser", "password123");
        System.out.println("Registration result: " + registered);
        
        // Now try to authenticate with wrong password
        System.out.println("\n--- Attempting authentication with wrong password ---");
        BankingUser user = processService.authenticateUser("testuser", "wrongpassword");
        System.out.println("Authentication result: " + (user != null ? "SUCCESS" : "FAILURE"));
        
        System.out.println("\n=== DIAGNOSTIC TEST END ===");
    }
}
