package com.example.banking.api.service;

import com.example.banking.api.config.BankingApplicationProperties;
import com.example.banking.api.model.BankingTransaction;
import com.example.banking.api.model.BankingUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service that interacts with the banking application JAR via process execution.
 * This service launches the banking application as a separate process and communicates
 * with it through stdin/stdout.
 */
@Service
public class BankingProcessService {
    
    private static final Logger logger = LoggerFactory.getLogger(BankingProcessService.class);
    
    private final BankingApplicationProperties properties;
    
    // Patterns for parsing output
    private static final Pattern BALANCE_PATTERN = Pattern.compile("Current Balance: \\$([0-9]+\\.?[0-9]*)");
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile("\\[([0-9-: ]+)\\] ([^:]+): \\$([0-9]+\\.?[0-9]*)");
    private static final DateTimeFormatter TRANSACTION_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    public BankingProcessService(BankingApplicationProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Register a new user by interacting with the banking application process.
     */
    public boolean registerUser(String username, String password) {
        try {
            Process process = startBankingProcess();
            
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                
                // Navigate to registration
                writer.write("2\n"); // Choose register option
                writer.flush();
                
                // Enter username
                writer.write(username + "\n");
                writer.flush();
                
                // Enter password
                writer.write(password + "\n");
                writer.flush();
                
                // Exit application
                writer.write("3\n"); // Exit from main menu
                writer.flush();
                
                // Read output to determine success
                String output = readProcessOutput(reader);
                logger.debug("Registration output: {}", output);
                
                boolean success = output.contains("Registration successful!");
                
                // Wait for process to complete
                process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);
                
                return success;
                
            } finally {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }
            
        } catch (Exception e) {
            logger.error("Error during user registration", e);
            return false;
        }
    }
    
    /**
     * Authenticate a user and return user information.
     */
    public BankingUser authenticateUser(String username, String password) {
        try {
            Process process = startBankingProcess();
            
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                
                // Navigate to login
                writer.write("1\n"); // Choose login option
                writer.flush();
                
                // Enter credentials
                writer.write(username + "\n");
                writer.flush();
                writer.write(password + "\n");
                writer.flush();
                
                // Read output to check authentication
                String output = readProcessOutput(reader);
                logger.debug("Authentication output: {}", output);
                
                if (output.contains("Welcome, " + username + "!")) {
                    // Authentication successful, get balance
                    // We're now in the banking menu, logout to get back to main menu
                    writer.write("4\n"); // Logout
                    writer.flush();
                    writer.write("3\n"); // Exit
                    writer.flush();
                    
                    // Parse balance from output if available
                    double balance = parseBalance(output);
                    
                    process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);
                    
                    return new BankingUser(username, balance);
                } else {
                    // Authentication failed
                    writer.write("3\n"); // Exit
                    writer.flush();
                    process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);
                    return null;
                }
                
            } finally {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }
            
        } catch (Exception e) {
            logger.error("Error during user authentication", e);
            return null;
        }
    }
    
    /**
     * Start the banking application process.
     */
    private Process startBankingProcess() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
            properties.getJavaCommand(), 
            "-jar", 
            properties.getJarPath()
        );
        
        // Set working directory to the banking-api directory to ensure proper file paths
        processBuilder.directory(new File("."));
        
        return processBuilder.start();
    }
    
    /**
     * Read all available output from the process.
     */
    private String readProcessOutput(BufferedReader reader) throws IOException {
        StringBuilder output = new StringBuilder();
        String line;
        
        // Read with a small delay to allow process to generate output
        try {
            Thread.sleep(1000); // Give process time to respond
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        while (reader.ready() && (line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        return output.toString();
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
    
    /**
     * Parse transactions from process output.
     */
    private List<BankingTransaction> parseTransactions(String output) {
        List<BankingTransaction> transactions = new ArrayList<>();
        Matcher matcher = TRANSACTION_PATTERN.matcher(output);
        
        while (matcher.find()) {
            try {
                String dateStr = matcher.group(1);
                String type = matcher.group(2);
                double amount = Double.parseDouble(matcher.group(3));
                
                LocalDateTime timestamp = LocalDateTime.parse(dateStr, TRANSACTION_DATE_FORMAT);
                transactions.add(new BankingTransaction(type, amount, timestamp));
                
            } catch (Exception e) {
                logger.warn("Failed to parse transaction: {}", matcher.group(0), e);
            }
        }
        
        return transactions;
    }

    /**
     * Perform a deposit operation.
     */
    public boolean deposit(String username, String password, double amount) {
        try {
            Process process = startBankingProcess();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                // Login first
                writer.write("1\n"); // Choose login option
                writer.flush();
                writer.write(username + "\n");
                writer.flush();
                writer.write(password + "\n");
                writer.flush();

                // Check if login was successful
                String loginOutput = readProcessOutput(reader);
                if (!loginOutput.contains("Welcome, " + username + "!")) {
                    writer.write("3\n"); // Exit
                    writer.flush();
                    return false;
                }

                // Perform deposit
                writer.write("1\n"); // Choose deposit option
                writer.flush();
                writer.write(String.valueOf(amount) + "\n");
                writer.flush();

                // Read deposit result
                String depositOutput = readProcessOutput(reader);
                boolean success = depositOutput.contains("Successfully deposited $" + amount);

                // Logout and exit
                writer.write("4\n"); // Logout
                writer.flush();
                writer.write("3\n"); // Exit
                writer.flush();

                process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);

                return success;

            } finally {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }

        } catch (Exception e) {
            logger.error("Error during deposit operation", e);
            return false;
        }
    }

    /**
     * Perform a withdrawal operation.
     */
    public boolean withdraw(String username, String password, double amount) {
        try {
            Process process = startBankingProcess();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                // Login first
                writer.write("1\n"); // Choose login option
                writer.flush();
                writer.write(username + "\n");
                writer.flush();
                writer.write(password + "\n");
                writer.flush();

                // Check if login was successful
                String loginOutput = readProcessOutput(reader);
                if (!loginOutput.contains("Welcome, " + username + "!")) {
                    writer.write("3\n"); // Exit
                    writer.flush();
                    return false;
                }

                // Perform withdrawal
                writer.write("2\n"); // Choose withdraw option
                writer.flush();
                writer.write(String.valueOf(amount) + "\n");
                writer.flush();

                // Read withdrawal result
                String withdrawOutput = readProcessOutput(reader);
                boolean success = withdrawOutput.contains("Successfully withdrew $" + amount);

                // Logout and exit
                writer.write("4\n"); // Logout
                writer.flush();
                writer.write("3\n"); // Exit
                writer.flush();

                process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);

                return success;

            } finally {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }

        } catch (Exception e) {
            logger.error("Error during withdrawal operation", e);
            return false;
        }
    }

    /**
     * Get account balance for a user.
     */
    public Double getBalance(String username, String password) {
        BankingUser user = authenticateUser(username, password);
        return user != null ? user.getBalance() : null;
    }

    /**
     * Get transaction history for a user.
     */
    public List<BankingTransaction> getTransactions(String username, String password) {
        try {
            Process process = startBankingProcess();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                // Login first
                writer.write("1\n"); // Choose login option
                writer.flush();
                writer.write(username + "\n");
                writer.flush();
                writer.write(password + "\n");
                writer.flush();

                // Check if login was successful
                String loginOutput = readProcessOutput(reader);
                if (!loginOutput.contains("Welcome, " + username + "!")) {
                    writer.write("3\n"); // Exit
                    writer.flush();
                    return null;
                }

                // List transactions
                writer.write("3\n"); // Choose list transactions option
                writer.flush();

                // Read transaction output
                String transactionOutput = readProcessOutput(reader);
                List<BankingTransaction> transactions = parseTransactions(transactionOutput);

                // Logout and exit
                writer.write("4\n"); // Logout
                writer.flush();
                writer.write("3\n"); // Exit
                writer.flush();

                process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);

                return transactions;

            } finally {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }

        } catch (Exception e) {
            logger.error("Error during transaction listing", e);
            return null;
        }
    }

    /**
     * Delete a user account.
     */
    public boolean deleteUser(String username, String password) {
        // Note: The original banking application doesn't have a delete user function in the UI
        // This would require extending the original application or implementing a workaround
        // For now, we'll return false to indicate this operation is not supported
        logger.warn("Delete user operation is not supported by the banking application CLI");
        return false;
    }
}
