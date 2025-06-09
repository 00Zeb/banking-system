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
    private final JarLocatorService jarLocatorService;

    // Patterns for parsing output
    private static final Pattern BALANCE_PATTERN = Pattern.compile("Current Balance: \\$([0-9]+\\.?[0-9]*)");
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile("\\[([0-9-: ]+)\\] ([^:]+): \\$([0-9]+\\.?[0-9]*)");
    private static final DateTimeFormatter TRANSACTION_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Pattern to remove ANSI escape codes
    private static final Pattern ANSI_ESCAPE_PATTERN = Pattern.compile("\\x1B\\[[;\\d]*[A-Za-z]|\\x1B\\][^\\x07]*\\x07|\\x1B\\[[?]?[0-9;]*[hlH]|\\x1B\\[\\d*[ABCD]|\\x1B\\[\\d*[JK]|\\x1B\\[\\d*;\\d*[Hf]|\\x1B\\[\\d*[mG]|\\x1B\\[\\d*[tT]|\\x1B\\[\\?\\d*[lh]|\\x1B\\[\\d*[PQRS]|\\x1B\\[\\d*[@]|\\x1B\\[\\d*[X]|\\x1B\\[\\d*[`]|\\x1B\\[\\d*[a-z]|\\x1B\\[\\d*[A-Z]|\\x1B\\[[0-9;]*[a-zA-Z]|\\x1B\\]0;[^\\x07]*\\x07");

    @Autowired
    public BankingProcessService(BankingApplicationProperties properties, JarLocatorService jarLocatorService) {
        this.properties = properties;
        this.jarLocatorService = jarLocatorService;
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

                // Wait for the main menu to appear
                String initialOutput = waitForPrompt(reader, "Choose an option:");
                logger.debug("Initial menu output: {}", initialOutput);

                // Navigate to login
                writer.write("1\n"); // Choose login option
                writer.flush();

                // Wait for username prompt
                String usernamePrompt = waitForPrompt(reader, "Username:");
                logger.debug("Username prompt output: {}", usernamePrompt);

                // Enter username
                writer.write(username + "\n");
                writer.flush();

                // Wait for password prompt
                String passwordPrompt = waitForPrompt(reader, "Password:");
                logger.debug("Password prompt output: {}", passwordPrompt);

                // Enter password
                writer.write(password + "\n");
                writer.flush();

                // Read authentication result
                String output = readProcessOutput(reader);
                logger.debug("Authentication output: {}", output);
                
                if (output.contains("Welcome, " + username + "!")) {
                    // Authentication successful
                    // We're now in the banking menu, wait for it to appear
                    String bankingMenuOutput = waitForPrompt(reader, "Please choose an option:");
                    logger.debug("Banking menu output: {}", bankingMenuOutput);

                    // Logout to get back to main menu
                    writer.write("4\n"); // Logout
                    writer.flush();

                    // Wait for main menu to appear again
                    String mainMenuOutput = waitForPrompt(reader, "Choose an option:");
                    logger.debug("Main menu after logout: {}", mainMenuOutput);

                    // Exit
                    writer.write("3\n"); // Exit
                    writer.flush();

                    // Parse balance from output if available (default to 0.0 since balance isn't shown in welcome)
                    double balance = 0.0;

                    process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);

                    return new BankingUser(username, balance);
                } else {
                    // Authentication failed, wait for main menu and exit
                    String mainMenuOutput = waitForPrompt(reader, "Choose an option:");
                    logger.debug("Main menu after failed auth: {}", mainMenuOutput);

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
     * Start the banking application process using the JAR located by JarLocatorService.
     */
    private Process startBankingProcess() throws IOException {
        String jarPath = jarLocatorService.getJarPath();

        if (jarPath == null || !jarLocatorService.isJarAccessible()) {
            throw new IOException("Banking application JAR not accessible: " + jarLocatorService.getJarInfo());
        }

        logger.debug("Starting banking process with JAR: {}", jarPath);

        ProcessBuilder processBuilder = new ProcessBuilder(
            properties.getJavaCommand(),
            "-jar",
            jarPath
        );

        // Set working directory to the current directory
        processBuilder.directory(new File("."));

        // Set environment variables to disable ANSI codes and terminal features
        processBuilder.environment().put("TERM", "dumb");
        processBuilder.environment().put("NO_COLOR", "1");
        processBuilder.environment().put("ANSI_COLORS_DISABLED", "1");

        return processBuilder.start();
    }

    /**
     * Clean output by removing ANSI escape codes and control characters.
     */
    private String cleanOutput(String rawOutput) {
        if (rawOutput == null) {
            return "";
        }

        // Remove ANSI escape codes
        String cleaned = ANSI_ESCAPE_PATTERN.matcher(rawOutput).replaceAll("");

        // Remove other control characters except newlines and tabs
        cleaned = cleaned.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");

        // Normalize line endings
        cleaned = cleaned.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");

        // Remove excessive whitespace but preserve structure
        cleaned = cleaned.replaceAll("[ \\t]+", " ");

        logger.debug("Cleaned output: [{}]", cleaned);
        return cleaned;
    }
    
    /**
     * Read all available output from the process with improved timing.
     */
    private String readProcessOutput(BufferedReader reader) throws IOException {
        StringBuilder output = new StringBuilder();
        String line;

        // Give process more time to generate output and try multiple times
        int attempts = 0;
        int maxAttempts = 10;

        while (attempts < maxAttempts) {
            try {
                Thread.sleep(500); // Wait 500ms between attempts
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            // Read all available lines
            boolean foundNewContent = false;
            while (reader.ready() && (line = reader.readLine()) != null) {
                output.append(line).append("\n");
                foundNewContent = true;
            }

            // If we found content, wait a bit more for any additional output
            if (foundNewContent) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // Read any additional lines that might have appeared
                while (reader.ready() && (line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                break; // We got output, so we're done
            }

            attempts++;
        }

        String result = cleanOutput(output.toString());
        logger.debug("Process output (cleaned): {}", result);
        return result;
    }

    /**
     * Wait for a specific prompt or text to appear in the output.
     */
    private String waitForPrompt(BufferedReader reader, String expectedPrompt) throws IOException {
        StringBuilder output = new StringBuilder();
        String line;

        int attempts = 0;
        int maxAttempts = 20; // Wait up to 10 seconds (20 * 500ms)

        while (attempts < maxAttempts) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            while (reader.ready() && (line = reader.readLine()) != null) {
                output.append(line).append("\n");
                String cleanedOutput = cleanOutput(output.toString());
                if (cleanedOutput.contains(expectedPrompt)) {
                    logger.debug("Found expected prompt: {}", expectedPrompt);
                    return cleanedOutput;
                }
            }

            attempts++;
        }

        String cleanedOutput = cleanOutput(output.toString());
        logger.debug("Timeout waiting for prompt: {}, got output: {}", expectedPrompt, cleanedOutput);
        return cleanedOutput;
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

                // Wait for main menu
                waitForPrompt(reader, "Choose an option:");

                // Login first
                writer.write("1\n"); // Choose login option
                writer.flush();

                waitForPrompt(reader, "Username:");
                writer.write(username + "\n");
                writer.flush();

                waitForPrompt(reader, "Password:");
                writer.write(password + "\n");
                writer.flush();

                // Check if login was successful
                String loginOutput = readProcessOutput(reader);
                if (!loginOutput.contains("Welcome, " + username + "!")) {
                    waitForPrompt(reader, "Choose an option:");
                    writer.write("3\n"); // Exit
                    writer.flush();
                    return false;
                }

                // Wait for banking menu
                waitForPrompt(reader, "Please choose an option:");

                // Perform deposit
                writer.write("1\n"); // Choose deposit option
                writer.flush();

                waitForPrompt(reader, "Enter amount to deposit:");
                writer.write(String.valueOf(amount) + "\n");
                writer.flush();

                // Read deposit result - look for deposit confirmation message
                String depositOutput = readProcessOutput(reader);
                // The Account class outputs "Successfully deposited $" + amount
                boolean success = depositOutput.contains("Successfully deposited $" + amount);

                // Wait for menu and logout
                waitForPrompt(reader, "Please choose an option:");
                writer.write("4\n"); // Logout
                writer.flush();

                waitForPrompt(reader, "Choose an option:");
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

                // Wait for main menu
                waitForPrompt(reader, "Choose an option:");

                // Login first
                writer.write("1\n"); // Choose login option
                writer.flush();

                waitForPrompt(reader, "Username:");
                writer.write(username + "\n");
                writer.flush();

                waitForPrompt(reader, "Password:");
                writer.write(password + "\n");
                writer.flush();

                // Check if login was successful
                String loginOutput = readProcessOutput(reader);
                if (!loginOutput.contains("Welcome, " + username + "!")) {
                    waitForPrompt(reader, "Choose an option:");
                    writer.write("3\n"); // Exit
                    writer.flush();
                    return false;
                }

                // Wait for banking menu
                waitForPrompt(reader, "Please choose an option:");

                // Perform withdrawal
                writer.write("2\n"); // Choose withdraw option
                writer.flush();

                waitForPrompt(reader, "Enter amount to withdraw:");
                writer.write(String.valueOf(amount) + "\n");
                writer.flush();

                // Read withdrawal result
                String withdrawOutput = readProcessOutput(reader);
                boolean success = withdrawOutput.contains("Successfully withdrew $" + amount);

                // Wait for menu and logout
                waitForPrompt(reader, "Please choose an option:");
                writer.write("4\n"); // Logout
                writer.flush();

                waitForPrompt(reader, "Choose an option:");
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
     * Get account balance for a user by checking transaction history.
     */
    public Double getBalance(String username, String password) {
        try {
            Process process = startBankingProcess();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                // Wait for main menu
                waitForPrompt(reader, "Choose an option:");

                // Login first
                writer.write("1\n"); // Choose login option
                writer.flush();

                waitForPrompt(reader, "Username:");
                writer.write(username + "\n");
                writer.flush();

                waitForPrompt(reader, "Password:");
                writer.write(password + "\n");
                writer.flush();

                // Check if login was successful
                String loginOutput = readProcessOutput(reader);
                if (!loginOutput.contains("Welcome, " + username + "!")) {
                    waitForPrompt(reader, "Choose an option:");
                    writer.write("3\n"); // Exit
                    writer.flush();
                    return null;
                }

                // Wait for banking menu
                waitForPrompt(reader, "Please choose an option:");

                // List transactions to see current balance
                writer.write("3\n"); // Choose list transactions option
                writer.flush();

                // Read transaction output which includes current balance
                String transactionOutput = readProcessOutput(reader);
                double balance = parseBalance(transactionOutput);

                // Wait for menu and logout
                waitForPrompt(reader, "Please choose an option:");
                writer.write("4\n"); // Logout
                writer.flush();

                waitForPrompt(reader, "Choose an option:");
                writer.write("3\n"); // Exit
                writer.flush();

                process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);

                return balance;

            } finally {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }

        } catch (Exception e) {
            logger.error("Error during balance retrieval", e);
            return null;
        }
    }

    /**
     * Get transaction history for a user.
     */
    public List<BankingTransaction> getTransactions(String username, String password) {
        try {
            Process process = startBankingProcess();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                // Wait for main menu
                waitForPrompt(reader, "Choose an option:");

                // Login first
                writer.write("1\n"); // Choose login option
                writer.flush();

                waitForPrompt(reader, "Username:");
                writer.write(username + "\n");
                writer.flush();

                waitForPrompt(reader, "Password:");
                writer.write(password + "\n");
                writer.flush();

                // Check if login was successful
                String loginOutput = readProcessOutput(reader);
                if (!loginOutput.contains("Welcome, " + username + "!")) {
                    waitForPrompt(reader, "Choose an option:");
                    writer.write("3\n"); // Exit
                    writer.flush();
                    return null;
                }

                // Wait for banking menu
                waitForPrompt(reader, "Please choose an option:");

                // List transactions
                writer.write("3\n"); // Choose list transactions option
                writer.flush();

                // Read transaction output
                String transactionOutput = readProcessOutput(reader);
                List<BankingTransaction> transactions = parseTransactions(transactionOutput);

                // Wait for menu and logout
                waitForPrompt(reader, "Please choose an option:");
                writer.write("4\n"); // Logout
                writer.flush();

                waitForPrompt(reader, "Choose an option:");
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
