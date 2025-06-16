package com.example.banking.api.service;

import com.example.banking.api.config.BankingApplicationProperties;
import com.example.banking.api.model.BankingTransaction;
import com.example.banking.api.model.BankingUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
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

    private Process process;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    /**
     * Register a new user by interacting with the banking application process.
     */
    public boolean registerUser(String username, String password) {
        try {
            startBankingProcess();
            
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
            startBankingProcess();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                logger.info("=== AUTHENTICATION DEBUG START ===");

                // Step 1: Wait for initial output and log everything
                logger.info("Step 1: Waiting for initial menu...");
                String initialOutput = readAllAvailableOutput(reader, 3000); // Wait 3 seconds
                logger.info("Step 1 - Raw initial output: [{}]", initialOutput);

                // Check if we have a menu
                if (!initialOutput.toLowerCase().contains("login") && !initialOutput.toLowerCase().contains("choose")) {
                    logger.error("No menu found in initial output");
                    return null;
                }

                // Step 2: Send login option
                logger.info("Step 2: Sending login option (1)...");
                writer.write("1\n");
                writer.flush();

                // Step 3: Wait for username prompt
                logger.info("Step 3: Waiting for username prompt...");
                String usernameOutput = readAllAvailableOutput(reader, 2000); // Wait 2 seconds
                logger.info("Step 3 - Raw username output: [{}]", usernameOutput);

                // Step 4: Send username
                logger.info("Step 4: Sending username: {}", username);
                writer.write(username + "\n");
                writer.flush();

                // Step 5: Wait for password prompt
                logger.info("Step 5: Waiting for password prompt...");
                String passwordOutput = readAllAvailableOutput(reader, 2000); // Wait 2 seconds
                logger.info("Step 5 - Raw password output: [{}]", passwordOutput);

                // Step 6: Send password
                logger.info("Step 6: Sending password...");
                writer.write(password + "\n");
                writer.flush();

                // Step 7: Wait for authentication result
                logger.info("Step 7: Waiting for authentication result...");
                String authResult = readAllAvailableOutput(reader, 3000); // Wait 3 seconds
                logger.info("Step 7 - Raw auth result: [{}]", authResult);

                // Step 8: Analyze the result
                logger.info("Step 8: Analyzing authentication result...");
                boolean isSuccessful = isAuthenticationSuccessful(authResult, username);
                logger.info("Step 8 - Authentication successful: {}", isSuccessful);

                if (isSuccessful) {
                    // Step 9: Handle successful login - try to logout gracefully
                    logger.info("Step 9: Attempting graceful logout...");

                    // Try to send logout command (4) and then exit (3)
                    writer.write("4\n"); // Logout
                    writer.flush();

                    String logoutOutput = readAllAvailableOutput(reader, 2000);
                    logger.info("Step 9 - Logout output: [{}]", logoutOutput);

                    writer.write("3\n"); // Exit
                    writer.flush();

                    process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);

                    logger.info("=== AUTHENTICATION DEBUG END - SUCCESS ===");
                    return new BankingUser(username, 0.0);
                } else {
                    // Step 10: Handle failed login
                    logger.info("Step 10: Handling failed authentication...");

                    // Try to exit gracefully
                    writer.write("3\n"); // Exit
                    writer.flush();

                    process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);

                    logger.info("=== AUTHENTICATION DEBUG END - FAILURE ===");
                    return null;
                }

            } finally {
                if (process.isAlive()) {
                    logger.info("Force killing process...");
                    process.destroyForcibly();
                }
            }

        } catch (Exception e) {
            logger.error("Error during user authentication", e);
            return null;
        }
    }

    /**
     * Read all available output within a time limit using optimized asynchronous I/O.
     * This replaces the slow Thread.sleep() approach with efficient polling and smart delays.
     */
    private String readAllAvailableOutput(BufferedReader reader, long timeoutMs) throws IOException {
        try {
            CompletableFuture<String> readFuture = CompletableFuture.supplyAsync(() -> {
                StringBuilder output = new StringBuilder();
                try {
                    long startTime = System.currentTimeMillis();
                    boolean hasData = false;
                    int consecutiveEmptyReads = 0;

                    // Initial small delay to let process start
                    Thread.sleep(100);

                    while (System.currentTimeMillis() - startTime < timeoutMs) {
                        // Check if data is available
                        if (reader.ready()) {
                            hasData = true;
                            consecutiveEmptyReads = 0;

                            // Read all available characters
                            while (reader.ready()) {
                                int ch = reader.read();
                                if (ch != -1) {
                                    output.append((char) ch);
                                }
                            }

                            // Small delay to allow more data to arrive
                            Thread.sleep(25);
                        } else {
                            consecutiveEmptyReads++;

                            // If we had data before and now there's none, we might be done
                            if (hasData && consecutiveEmptyReads > 3) {
                                break;
                            }

                            // Adaptive delay - start small and increase slightly
                            long delay = Math.min(50, 10 + consecutiveEmptyReads * 5);
                            Thread.sleep(delay);
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Error reading process output: {}", e.getMessage());
                }
                return output.toString();
            }, executorService);

            String result = readFuture.get(timeoutMs + 500, TimeUnit.MILLISECONDS);
            logger.debug("Raw output: [{}]", result);
            return result;

        } catch (TimeoutException e) {
            logger.debug("Timeout reading process output after {}ms", timeoutMs);
            return "";
        } catch (Exception e) {
            logger.debug("Error reading process output: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Determine if authentication was successful based on the output.
     */
    private boolean isAuthenticationSuccessful(String output, String username) {
        if (output == null || output.trim().isEmpty()) {
            logger.debug("No output to analyze");
            return false;
        }

        String lowerOutput = output.toLowerCase();

        // Look for various success indicators based on actual banking app output
        boolean hasWelcome = lowerOutput.contains("welcome");
        boolean hasUsername = lowerOutput.contains(username.toLowerCase());
        boolean hasSuccessfulLogin = lowerOutput.contains("successful");
        boolean hasLoggedIn = lowerOutput.contains("logged in");
        boolean hasBankingMenu = lowerOutput.contains("deposit") || lowerOutput.contains("withdraw") || lowerOutput.contains("balance");
        boolean hasAccountMenu = lowerOutput.contains("please choose an option") && (lowerOutput.contains("deposit") || lowerOutput.contains("withdraw"));

        // Look for failure indicators
        boolean hasInvalidCredentials = lowerOutput.contains("invalid") || lowerOutput.contains("incorrect") || lowerOutput.contains("wrong");
        boolean hasLoginFailed = lowerOutput.contains("failed") || lowerOutput.contains("error");
        boolean hasAccessDenied = lowerOutput.contains("access denied") || lowerOutput.contains("authentication failed");

        logger.debug("Authentication analysis - Welcome: {}, Username: {}, Successful: {}, LoggedIn: {}, BankingMenu: {}, AccountMenu: {}, Invalid: {}, Failed: {}, AccessDenied: {}",
                    hasWelcome, hasUsername, hasSuccessfulLogin, hasLoggedIn, hasBankingMenu, hasAccountMenu, hasInvalidCredentials, hasLoginFailed, hasAccessDenied);

        // If we have clear failure indicators, it's a failure
        if (hasInvalidCredentials || hasLoginFailed || hasAccessDenied) {
            return false;
        }

        // If we have success indicators, it's a success
        if ((hasWelcome && hasUsername) || hasSuccessfulLogin || hasLoggedIn || hasBankingMenu || hasAccountMenu) {
            return true;
        }

        // Default to failure if we can't determine
        return false;
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

        this.process = processBuilder.start();
        return this.process;
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
     * Read all available output from the process using asynchronous I/O.
     * This replaces the slow polling approach with efficient event-driven reading.
     */
    private String readProcessOutput(BufferedReader reader) throws IOException {
        // Use the fast async method with a reasonable timeout
        String rawOutput = readAllAvailableOutput(reader, 2000);
        String result = cleanOutput(rawOutput);
        logger.debug("Process output (cleaned): {}", result);
        return result;
    }

    /**
     * Wait for a specific prompt or text to appear in the output using optimized asynchronous I/O.
     * This replaces the slow polling approach with efficient reading and smart delays.
     */
    private String waitForPrompt(BufferedReader reader, String expectedPrompt) throws IOException {
        try {
            CompletableFuture<String> promptFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    StringBuilder output = new StringBuilder();
                    long startTime = System.currentTimeMillis();
                    long maxWaitTime = 3000; // 3 seconds max
                    int consecutiveEmptyReads = 0;

                    // Initial small delay
                    Thread.sleep(50);

                    while (System.currentTimeMillis() - startTime < maxWaitTime) {
                        if (reader.ready()) {
                            consecutiveEmptyReads = 0;

                            // Read all available characters
                            while (reader.ready()) {
                                int ch = reader.read();
                                if (ch != -1) {
                                    output.append((char) ch);
                                }
                            }

                            String cleanedOutput = cleanOutput(output.toString());
                            if (cleanedOutput.contains(expectedPrompt)) {
                                logger.debug("Found expected prompt: {}", expectedPrompt);
                                return cleanedOutput;
                            }

                            // Small delay after reading
                            Thread.sleep(25);
                        } else {
                            consecutiveEmptyReads++;

                            // Adaptive delay
                            long delay = Math.min(100, 25 + consecutiveEmptyReads * 10);
                            Thread.sleep(delay);
                        }
                    }

                    String cleanedOutput = cleanOutput(output.toString());
                    logger.debug("Timeout waiting for prompt: {}, got output: {}", expectedPrompt, cleanedOutput);
                    return cleanedOutput;

                } catch (Exception e) {
                    logger.debug("Error waiting for prompt: {}", e.getMessage());
                    return "";
                }
            }, executorService);

            return promptFuture.get(4, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            logger.debug("Timeout waiting for prompt: {}", expectedPrompt);
            return "";
        } catch (Exception e) {
            logger.debug("Error waiting for prompt: {}", e.getMessage());
            return "";
        }
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
            startBankingProcess();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                logger.info("=== DEPOSIT OPERATION DEBUG START ===");

                // Step 1: Wait for initial output and log everything
                logger.info("Step 1: Waiting for initial menu...");
                String initialOutput = readAllAvailableOutput(reader, 3000); // Wait 3 seconds
                logger.info("Step 1 - Raw initial output: [{}]", initialOutput);

                // Check if we have a menu
                if (!initialOutput.toLowerCase().contains("login") && !initialOutput.toLowerCase().contains("choose")) {
                    logger.error("No menu found in initial output");
                    return false;
                }

                // Step 2: Send login option
                logger.info("Step 2: Sending login option (1)...");
                writer.write("1\n");
                writer.flush();

                // Step 3: Wait for username prompt
                logger.info("Step 3: Waiting for username prompt...");
                String usernameOutput = readAllAvailableOutput(reader, 2000); // Wait 2 seconds
                logger.info("Step 3 - Raw username output: [{}]", usernameOutput);

                // Step 4: Send username
                logger.info("Step 4: Sending username: {}", username);
                writer.write(username + "\n");
                writer.flush();

                // Step 5: Wait for password prompt
                logger.info("Step 5: Waiting for password prompt...");
                String passwordOutput = readAllAvailableOutput(reader, 2000); // Wait 2 seconds
                logger.info("Step 5 - Raw password output: [{}]", passwordOutput);

                // Step 6: Send password
                logger.info("Step 6: Sending password...");
                writer.write(password + "\n");
                writer.flush();

                // Step 7: Wait for authentication result
                logger.info("Step 7: Waiting for authentication result...");
                String authResult = readAllAvailableOutput(reader, 3000); // Wait 3 seconds
                logger.info("Step 7 - Raw auth result: [{}]", authResult);

                // Step 8: Analyze the result
                logger.info("Step 8: Analyzing authentication result...");
                boolean isSuccessful = isAuthenticationSuccessful(authResult, username);
                logger.info("Step 8 - Authentication successful: {}", isSuccessful);

                if (!isSuccessful) {
                    logger.info("Step 8: Authentication failed, exiting...");
                    writer.write("3\n"); // Exit
                    writer.flush();
                    process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);
                    return false;
                }

                // Step 9: Wait for banking menu and perform deposit
                logger.info("Step 9: Waiting for banking menu...");
                String bankingMenuOutput = readAllAvailableOutput(reader, 2000);
                logger.info("Step 9 - Banking menu output: [{}]", bankingMenuOutput);

                // Step 10: Send deposit option
                logger.info("Step 10: Sending deposit option (1)...");
                writer.write("1\n"); // Choose deposit option
                writer.flush();

                // Step 11: Wait for deposit amount prompt
                logger.info("Step 11: Waiting for deposit amount prompt...");
                String depositPromptOutput = readAllAvailableOutput(reader, 2000);
                logger.info("Step 11 - Deposit prompt output: [{}]", depositPromptOutput);

                // Step 12: Send deposit amount
                logger.info("Step 12: Sending deposit amount: {}", amount);
                writer.write(String.valueOf(amount) + "\n");
                writer.flush();

                // Step 13: Wait for deposit result
                logger.info("Step 13: Waiting for deposit result...");
                String depositResult = readAllAvailableOutput(reader, 3000);
                logger.info("Step 13 - Deposit result: [{}]", depositResult);

                // Step 14: Check if deposit was successful
                boolean depositSuccess = depositResult.toLowerCase().contains("successfully deposited") ||
                                       depositResult.toLowerCase().contains("deposit successful") ||
                                       depositResult.contains("Successfully deposited $" + amount);
                logger.info("Step 14 - Deposit successful: {}", depositSuccess);

                // Step 15: Logout gracefully
                logger.info("Step 15: Attempting graceful logout...");
                writer.write("4\n"); // Logout
                writer.flush();

                String logoutOutput = readAllAvailableOutput(reader, 2000);
                logger.info("Step 15 - Logout output: [{}]", logoutOutput);

                writer.write("3\n"); // Exit
                writer.flush();

                process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);

                logger.info("=== DEPOSIT OPERATION DEBUG END - SUCCESS: {} ===", depositSuccess);
                return depositSuccess;

            } finally {
                if (process.isAlive()) {
                    logger.info("Force killing process...");
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
            startBankingProcess();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                logger.info("=== WITHDRAWAL OPERATION DEBUG START ===");

                // Step 1: Wait for initial output and log everything
                logger.info("Step 1: Waiting for initial menu...");
                String initialOutput = readAllAvailableOutput(reader, 3000); // Wait 3 seconds
                logger.info("Step 1 - Raw initial output: [{}]", initialOutput);

                // Check if we have a menu
                if (!initialOutput.toLowerCase().contains("login") && !initialOutput.toLowerCase().contains("choose")) {
                    logger.error("No menu found in initial output");
                    return false;
                }

                // Step 2: Send login option
                logger.info("Step 2: Sending login option (1)...");
                writer.write("1\n");
                writer.flush();

                // Step 3: Wait for username prompt
                logger.info("Step 3: Waiting for username prompt...");
                String usernameOutput = readAllAvailableOutput(reader, 2000); // Wait 2 seconds
                logger.info("Step 3 - Raw username output: [{}]", usernameOutput);

                // Step 4: Send username
                logger.info("Step 4: Sending username: {}", username);
                writer.write(username + "\n");
                writer.flush();

                // Step 5: Wait for password prompt
                logger.info("Step 5: Waiting for password prompt...");
                String passwordOutput = readAllAvailableOutput(reader, 2000); // Wait 2 seconds
                logger.info("Step 5 - Raw password output: [{}]", passwordOutput);

                // Step 6: Send password
                logger.info("Step 6: Sending password...");
                writer.write(password + "\n");
                writer.flush();

                // Step 7: Wait for authentication result
                logger.info("Step 7: Waiting for authentication result...");
                String authResult = readAllAvailableOutput(reader, 3000); // Wait 3 seconds
                logger.info("Step 7 - Raw auth result: [{}]", authResult);

                // Step 8: Analyze the result
                logger.info("Step 8: Analyzing authentication result...");
                boolean isSuccessful = isAuthenticationSuccessful(authResult, username);
                logger.info("Step 8 - Authentication successful: {}", isSuccessful);

                if (!isSuccessful) {
                    logger.info("Step 8: Authentication failed, exiting...");
                    writer.write("3\n"); // Exit
                    writer.flush();
                    process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);
                    return false;
                }

                // Step 9: Wait for banking menu and perform withdrawal
                logger.info("Step 9: Waiting for banking menu...");
                String bankingMenuOutput = readAllAvailableOutput(reader, 2000);
                logger.info("Step 9 - Banking menu output: [{}]", bankingMenuOutput);

                // Step 10: Send withdrawal option
                logger.info("Step 10: Sending withdrawal option (2)...");
                writer.write("2\n"); // Choose withdraw option
                writer.flush();

                // Step 11: Wait for withdrawal amount prompt
                logger.info("Step 11: Waiting for withdrawal amount prompt...");
                String withdrawPromptOutput = readAllAvailableOutput(reader, 2000);
                logger.info("Step 11 - Withdrawal prompt output: [{}]", withdrawPromptOutput);

                // Step 12: Send withdrawal amount
                logger.info("Step 12: Sending withdrawal amount: {}", amount);
                writer.write(String.valueOf(amount) + "\n");
                writer.flush();

                // Step 13: Wait for withdrawal result
                logger.info("Step 13: Waiting for withdrawal result...");
                String withdrawResult = readAllAvailableOutput(reader, 3000);
                logger.info("Step 13 - Withdrawal result: [{}]", withdrawResult);

                // Step 14: Check if withdrawal was successful
                boolean withdrawSuccess = withdrawResult.toLowerCase().contains("successfully withdrew") ||
                                        withdrawResult.toLowerCase().contains("withdrawal successful") ||
                                        withdrawResult.contains("Successfully withdrew $" + amount);
                logger.info("Step 14 - Withdrawal successful: {}", withdrawSuccess);

                // Step 15: Logout gracefully
                logger.info("Step 15: Attempting graceful logout...");
                writer.write("4\n"); // Logout
                writer.flush();

                String logoutOutput = readAllAvailableOutput(reader, 2000);
                logger.info("Step 15 - Logout output: [{}]", logoutOutput);

                writer.write("3\n"); // Exit
                writer.flush();

                process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);

                logger.info("=== WITHDRAWAL OPERATION DEBUG END - SUCCESS: {} ===", withdrawSuccess);
                return withdrawSuccess;

            } finally {
                if (process.isAlive()) {
                    logger.info("Force killing process...");
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
            startBankingProcess();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                logger.info("=== BALANCE OPERATION DEBUG START ===");

                // Step 1: Wait for initial output and log everything
                logger.info("Step 1: Waiting for initial menu...");
                String initialOutput = readAllAvailableOutput(reader, 3000); // Wait 3 seconds
                logger.info("Step 1 - Raw initial output: [{}]", initialOutput);

                // Check if we have a menu
                if (!initialOutput.toLowerCase().contains("login") && !initialOutput.toLowerCase().contains("choose")) {
                    logger.error("No menu found in initial output");
                    return null;
                }

                // Step 2: Send login option
                logger.info("Step 2: Sending login option (1)...");
                writer.write("1\n");
                writer.flush();

                // Step 3: Wait for username prompt
                logger.info("Step 3: Waiting for username prompt...");
                String usernameOutput = readAllAvailableOutput(reader, 2000); // Wait 2 seconds
                logger.info("Step 3 - Raw username output: [{}]", usernameOutput);

                // Step 4: Send username
                logger.info("Step 4: Sending username: {}", username);
                writer.write(username + "\n");
                writer.flush();

                // Step 5: Wait for password prompt
                logger.info("Step 5: Waiting for password prompt...");
                String passwordOutput = readAllAvailableOutput(reader, 2000); // Wait 2 seconds
                logger.info("Step 5 - Raw password output: [{}]", passwordOutput);

                // Step 6: Send password
                logger.info("Step 6: Sending password...");
                writer.write(password + "\n");
                writer.flush();

                // Step 7: Wait for authentication result
                logger.info("Step 7: Waiting for authentication result...");
                String authResult = readAllAvailableOutput(reader, 3000); // Wait 3 seconds
                logger.info("Step 7 - Raw auth result: [{}]", authResult);

                // Step 8: Analyze the result
                logger.info("Step 8: Analyzing authentication result...");
                boolean isSuccessful = isAuthenticationSuccessful(authResult, username);
                logger.info("Step 8 - Authentication successful: {}", isSuccessful);

                if (!isSuccessful) {
                    logger.info("Step 8: Authentication failed, exiting...");
                    writer.write("3\n"); // Exit
                    writer.flush();
                    process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);
                    return null;
                }

                // Step 9: Wait for banking menu and list transactions
                logger.info("Step 9: Waiting for banking menu...");
                String bankingMenuOutput = readAllAvailableOutput(reader, 2000);
                logger.info("Step 9 - Banking menu output: [{}]", bankingMenuOutput);

                // Step 10: Send list transactions option
                logger.info("Step 10: Sending list transactions option (3)...");
                writer.write("3\n"); // Choose list transactions option
                writer.flush();

                // Step 11: Wait for transaction output
                logger.info("Step 11: Waiting for transaction output...");
                String transactionOutput = readAllAvailableOutput(reader, 3000);
                logger.info("Step 11 - Transaction output: [{}]", transactionOutput);

                // Step 12: Parse balance from output
                double balance = parseBalance(transactionOutput);
                logger.info("Step 12 - Parsed balance: {}", balance);

                // Step 13: Logout gracefully
                logger.info("Step 13: Attempting graceful logout...");
                writer.write("4\n"); // Logout
                writer.flush();

                String logoutOutput = readAllAvailableOutput(reader, 2000);
                logger.info("Step 13 - Logout output: [{}]", logoutOutput);

                writer.write("3\n"); // Exit
                writer.flush();

                process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);

                logger.info("=== BALANCE OPERATION DEBUG END - BALANCE: {} ===", balance);
                return balance;

            } finally {
                if (process.isAlive()) {
                    logger.info("Force killing process...");
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
            startBankingProcess();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                logger.info("=== TRANSACTIONS OPERATION DEBUG START ===");

                // Step 1: Wait for initial output and log everything
                logger.info("Step 1: Waiting for initial menu...");
                String initialOutput = readAllAvailableOutput(reader, 3000); // Wait 3 seconds
                logger.info("Step 1 - Raw initial output: [{}]", initialOutput);

                // Check if we have a menu
                if (!initialOutput.toLowerCase().contains("login") && !initialOutput.toLowerCase().contains("choose")) {
                    logger.error("No menu found in initial output");
                    return null;
                }

                // Step 2: Send login option
                logger.info("Step 2: Sending login option (1)...");
                writer.write("1\n");
                writer.flush();

                // Step 3: Wait for username prompt
                logger.info("Step 3: Waiting for username prompt...");
                String usernameOutput = readAllAvailableOutput(reader, 2000); // Wait 2 seconds
                logger.info("Step 3 - Raw username output: [{}]", usernameOutput);

                // Step 4: Send username
                logger.info("Step 4: Sending username: {}", username);
                writer.write(username + "\n");
                writer.flush();

                // Step 5: Wait for password prompt
                logger.info("Step 5: Waiting for password prompt...");
                String passwordOutput = readAllAvailableOutput(reader, 2000); // Wait 2 seconds
                logger.info("Step 5 - Raw password output: [{}]", passwordOutput);

                // Step 6: Send password
                logger.info("Step 6: Sending password...");
                writer.write(password + "\n");
                writer.flush();

                // Step 7: Wait for authentication result
                logger.info("Step 7: Waiting for authentication result...");
                String authResult = readAllAvailableOutput(reader, 3000); // Wait 3 seconds
                logger.info("Step 7 - Raw auth result: [{}]", authResult);

                // Step 8: Analyze the result
                logger.info("Step 8: Analyzing authentication result...");
                boolean isSuccessful = isAuthenticationSuccessful(authResult, username);
                logger.info("Step 8 - Authentication successful: {}", isSuccessful);

                if (!isSuccessful) {
                    logger.info("Step 8: Authentication failed, exiting...");
                    writer.write("3\n"); // Exit
                    writer.flush();
                    process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);
                    return null;
                }

                // Step 9: Wait for banking menu and list transactions
                logger.info("Step 9: Waiting for banking menu...");
                String bankingMenuOutput = readAllAvailableOutput(reader, 2000);
                logger.info("Step 9 - Banking menu output: [{}]", bankingMenuOutput);

                // Step 10: Send list transactions option
                logger.info("Step 10: Sending list transactions option (3)...");
                writer.write("3\n"); // Choose list transactions option
                writer.flush();

                // Step 11: Wait for transaction output
                logger.info("Step 11: Waiting for transaction output...");
                String transactionOutput = readAllAvailableOutput(reader, 3000);
                logger.info("Step 11 - Transaction output: [{}]", transactionOutput);

                // Step 12: Parse transactions from output
                List<BankingTransaction> transactions = parseTransactions(transactionOutput);
                logger.info("Step 12 - Parsed {} transactions", transactions.size());

                // Step 13: Logout gracefully
                logger.info("Step 13: Attempting graceful logout...");
                writer.write("4\n"); // Logout
                writer.flush();

                String logoutOutput = readAllAvailableOutput(reader, 2000);
                logger.info("Step 13 - Logout output: [{}]", logoutOutput);

                writer.write("3\n"); // Exit
                writer.flush();

                process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);

                logger.info("=== TRANSACTIONS OPERATION DEBUG END - COUNT: {} ===", transactions.size());
                return transactions;

            } finally {
                if (process.isAlive()) {
                    logger.info("Force killing process...");
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

    /**
     * Cleanup resources when the service is destroyed.
     */
    @PreDestroy
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            logger.info("Shutting down executor service...");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (process != null && process.isAlive()) {
            logger.info("Terminating banking process...");
            process.destroyForcibly();
        }
    }
}
