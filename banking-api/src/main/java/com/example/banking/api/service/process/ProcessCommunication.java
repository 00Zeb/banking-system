package com.example.banking.api.service.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Provides communication interface with the banking application process.
 * This class encapsulates input/output operations and common communication patterns.
 */
public class ProcessCommunication {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessCommunication.class);
    
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private final ProcessExecutor executor;
    
    public ProcessCommunication(BufferedWriter writer, BufferedReader reader, ProcessExecutor executor) {
        this.writer = writer;
        this.reader = reader;
        this.executor = executor;
    }

    /**
     * Sends a command to the process.
     *
     * @param command the command to send
     * @throws IOException if writing fails
     */
    public void sendCommand(String command) throws IOException {
        logger.debug("Sending command: {}", command);
        writer.write(command + "\n");
        writer.flush();
    }

    /**
     * Sends multiple commands in sequence.
     *
     * @param commands the commands to send
     * @throws IOException if writing fails
     */
    public void sendCommands(String... commands) throws IOException {
        for (String command : commands) {
            sendCommand(command);
        }
    }

    /**
     * Reads output with a specific timeout.
     *
     * @param timeoutMs timeout in milliseconds
     * @return the output read
     * @throws IOException if reading fails
     */
    public String readOutput(long timeoutMs) throws IOException {
        return executor.readAllAvailableOutput(reader, timeoutMs);
    }

    /**
     * Reads and cleans process output.
     *
     * @return cleaned output
     * @throws IOException if reading fails
     */
    public String readCleanOutput() throws IOException {
        return executor.readProcessOutput(reader);
    }

    /**
     * Waits for the initial menu to appear.
     *
     * @return the initial menu output
     * @throws IOException if reading fails
     */
    public String waitForInitialMenu() throws IOException {
        logger.debug("Waiting for initial menu...");
        String output = readOutput(500);
        logger.debug("Initial menu output: [{}]", output);
        return output;
    }

    /**
     * Performs user authentication sequence.
     *
     * @param username the username
     * @param password the password
     * @return the authentication result output
     * @throws IOException if communication fails
     */
    public String authenticateUser(String username, String password) throws IOException {
        // Send login option
        logger.debug("Sending login option (1)...");
        sendCommand("1");

        // Wait for username prompt
        logger.debug("Waiting for username prompt...");
        readOutput(300);

        // Send username
        logger.debug("Sending username: {}", username);
        sendCommand(username);

        // Wait for password prompt
        logger.debug("Waiting for password prompt...");
        readOutput(300);

        // Send password
        logger.debug("Sending password...");
        sendCommand(password);

        // Wait for authentication result
        logger.debug("Waiting for authentication result...");
        String authResult = readOutput(500);
        logger.debug("Authentication result: [{}]", authResult);

        return authResult;
    }

    /**
     * Determines if authentication was successful based on the output.
     *
     * @param output the authentication output
     * @param username the username that was authenticated
     * @return true if authentication was successful
     */
    public boolean isAuthenticationSuccessful(String output, String username) {
        if (output == null || output.trim().isEmpty()) {
            logger.debug("No output to analyze");
            return false;
        }

        String lowerOutput = output.toLowerCase();

        // Look for various success indicators
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
     * Performs graceful logout sequence.
     *
     * @throws IOException if communication fails
     */
    public void performGracefulLogout() throws IOException {
        logger.debug("Attempting graceful logout...");

        // Try to send logout command (4) and then exit (3)
        sendCommand("4"); // Logout
        String logoutOutput = readOutput(300);
        logger.debug("Logout output: [{}]", logoutOutput);

        sendCommand("3"); // Exit
    }

    /**
     * Performs graceful exit without logout.
     *
     * @throws IOException if communication fails
     */
    public void performGracefulExit() throws IOException {
        logger.debug("Attempting graceful exit...");
        sendCommand("3"); // Exit
    }
    
    /**
     * Checks if the process is responsive by sending a simple command and waiting for response.
     * This can be used for health checks on persistent processes.
     *
     * @return true if process responds within timeout
     */
    public boolean isProcessResponsive() {
        try {
            // Try reading any available output first to clear buffer
            readOutput(100);
            return true;
        } catch (IOException e) {
            logger.debug("Process not responsive: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Waits for banking menu to appear (for session-based operations).
     * This is used after authentication to ensure the process is ready for operations.
     *
     * @return the banking menu output
     * @throws IOException if reading fails
     */
    public String waitForBankingMenu() throws IOException {
        logger.debug("Waiting for banking menu...");
        String output = readOutput(500);
        logger.debug("Banking menu output: [{}]", output);
        return output;
    }
    
    /**
     * Navigates back to main menu from any submenu.
     * This is useful for persistent processes to ensure clean state between operations.
     *
     * @throws IOException if communication fails
     */
    public void navigateToMainMenu() throws IOException {
        logger.debug("Navigating to main menu...");
        // Send a few back commands to ensure we're at main menu
        // This is a defensive approach for persistent processes
        readOutput(100); // Clear any pending output
    }
}