package com.example.banking.api.service.process;

import com.example.banking.api.config.BankingApplicationProperties;
import com.example.banking.api.service.JarLocatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.io.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * Service responsible for executing and communicating with the banking application process.
 * This service extracts common process execution logic to eliminate code duplication.
 */
@Service
public class ProcessExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ProcessExecutor.class);

    private final BankingApplicationProperties properties;
    private final JarLocatorService jarLocatorService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // Pattern to remove ANSI escape codes
    private static final Pattern ANSI_ESCAPE_PATTERN = Pattern.compile(
            "\\x1B\\[[;\\d]*[A-Za-z]|\\x1B\\][^\\x07]*\\x07|\\x1B\\[[?]?[0-9;]*[hlH]|" +
            "\\x1B\\[\\d*[ABCD]|\\x1B\\[\\d*[JK]|\\x1B\\[\\d*;\\d*[Hf]|\\x1B\\[\\d*[mG]|" +
            "\\x1B\\[\\d*[tT]|\\x1B\\[\\?\\d*[lh]|\\x1B\\[\\d*[PQRS]|\\x1B\\[\\d*[@]|" +
            "\\x1B\\[\\d*[X]|\\x1B\\[\\d*[`]|\\x1B\\[\\d*[a-z]|\\x1B\\[\\d*[A-Z]|" +
            "\\x1B\\[[0-9;]*[a-zA-Z]|\\x1B\\]0;[^\\x07]*\\x07"
    );

    public ProcessExecutor(BankingApplicationProperties properties, JarLocatorService jarLocatorService) {
        this.properties = properties;
        this.jarLocatorService = jarLocatorService;
    }

    /**
     * Executes a process operation with the banking application.
     *
     * @param operation The operation to execute
     * @return The result of the operation
     * @throws ProcessExecutionException if the operation fails
     */
    public <T> T execute(ProcessOperation<T> operation) {
        Process process = null;
        try {
            process = startBankingProcess();
            
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                
                return operation.execute(new ProcessCommunication(writer, reader, this));
                
            } finally {
                if (process.isAlive()) {
                    process.waitFor(properties.getProcessTimeout(), TimeUnit.MILLISECONDS);
                    if (process.isAlive()) {
                        process.destroyForcibly();
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error executing process operation", e);
            throw new ProcessExecutionException("Failed to execute process operation", e);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * Starts the banking application process.
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
     * Reads all available output using event-driven I/O without sleep.
     *
     * @param reader the reader to read from
     * @param timeoutMs timeout in milliseconds
     * @return the output read
     */
    public String readAllAvailableOutput(BufferedReader reader, long timeoutMs) throws IOException {
        try {
            CompletableFuture<String> readFuture = CompletableFuture.supplyAsync(() -> {
                StringBuilder output = new StringBuilder();
                try {
                    long startTime = System.currentTimeMillis();
                    boolean hasData = false;
                    int emptyReadCount = 0;

                    // Small initial delay only for the first read to let process start
                    if (timeoutMs >= 500) { // Only for initial menu reads
                        Thread.sleep(200); // Minimal delay for process startup
                    }

                    while (System.currentTimeMillis() - startTime < timeoutMs) {
                        // Check if data is available - NO SLEEP!
                        if (reader.ready()) {
                            hasData = true;
                            emptyReadCount = 0;

                            // Read all available characters immediately
                            while (reader.ready()) {
                                int ch = reader.read();
                                if (ch != -1) {
                                    output.append((char) ch);
                                }
                            }
                        } else {
                            emptyReadCount++;

                            // If we had data and now there's none for a while, we're probably done
                            if (hasData && emptyReadCount > 1000) { // ~1ms of empty reads
                                break;
                            }

                            // Yield CPU to other threads but don't sleep
                            Thread.yield();
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Error reading process output: {}", e.getMessage());
                }
                return output.toString();
            }, executorService);

            String result = readFuture.get(timeoutMs + 100, TimeUnit.MILLISECONDS);
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
     * Cleans output by removing ANSI escape codes and control characters.
     *
     * @param rawOutput the raw output to clean
     * @return cleaned output
     */
    public String cleanOutput(String rawOutput) {
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
     * Reads process output and cleans it.
     *
     * @param reader the reader to read from
     * @return cleaned output
     */
    public String readProcessOutput(BufferedReader reader) throws IOException {
        String rawOutput = readAllAvailableOutput(reader, 1000);
        String result = cleanOutput(rawOutput);
        logger.debug("Process output (cleaned): {}", result);
        return result;
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
    }
}