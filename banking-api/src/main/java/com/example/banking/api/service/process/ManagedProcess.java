package com.example.banking.api.service.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Represents a managed banking process with its I/O streams and metadata.
 * This class encapsulates a persistent process that can be reused across multiple operations.
 */
public class ManagedProcess {
    
    private static final Logger logger = LoggerFactory.getLogger(ManagedProcess.class);
    
    private final String processId;
    private final Process process;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private final String username;
    private final String sessionId;
    private final LocalDateTime createdAt;
    private volatile LocalDateTime lastAccessedAt;
    private volatile long lastAccessedTimeMs;
    private volatile boolean terminated = false;
    
    public ManagedProcess(String processId, Process process, BufferedWriter writer, 
                         BufferedReader reader, String username, String sessionId) {
        this.processId = processId;
        this.process = process;
        this.writer = writer;
        this.reader = reader;
        this.username = username;
        this.sessionId = sessionId;
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
        this.lastAccessedTimeMs = System.currentTimeMillis();
    }
    
    /**
     * Gets the unique process identifier
     */
    public String getProcessId() {
        return processId;
    }
    
    /**
     * Gets the underlying Process object
     */
    public Process getProcess() {
        return process;
    }
    
    /**
     * Gets the writer for sending commands to the process
     */
    public BufferedWriter getWriter() {
        return writer;
    }
    
    /**
     * Gets the reader for receiving output from the process
     */
    public BufferedReader getReader() {
        return reader;
    }
    
    /**
     * Gets the username associated with this process
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Gets the session ID associated with this process
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Gets the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Gets the last accessed timestamp
     */
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    /**
     * Gets the last accessed time in milliseconds (for timeout calculations)
     */
    public long getLastAccessedTime() {
        return lastAccessedTimeMs;
    }
    
    /**
     * Updates the last accessed timestamp
     */
    public void updateLastAccessed() {
        this.lastAccessedAt = LocalDateTime.now();
        this.lastAccessedTimeMs = System.currentTimeMillis();
    }
    
    /**
     * Checks if the process is healthy (alive and not terminated)
     */
    public boolean isHealthy() {
        return !terminated && process != null && process.isAlive();
    }
    
    /**
     * Checks if the process is idle (not accessed recently)
     */
    public boolean isIdle() {
        // Process is considered idle if not accessed for more than 1 minute
        return lastAccessedAt.plusMinutes(1).isBefore(LocalDateTime.now());
    }
    
    /**
     * Checks if the process has been idle for longer than the specified timeout
     */
    public boolean isIdleTimeout(long timeoutMs) {
        long idleTime = System.currentTimeMillis() - getLastAccessedTime();
        return idleTime > timeoutMs;
    }
    
    /**
     * Gets the duration this process has been running in seconds
     */
    public long getUptimeSeconds() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).getSeconds();
    }
    
    /**
     * Creates a ProcessCommunication instance for this managed process
     */
    public ProcessCommunication createCommunication(ProcessExecutor processExecutor) {
        if (!isHealthy()) {
            throw new IllegalStateException("Cannot create communication for unhealthy process");
        }
        updateLastAccessed();
        return new ProcessCommunication(writer, reader, processExecutor);
    }
    
    /**
     * Terminates the managed process and closes all resources
     */
    public void terminate() {
        if (terminated) {
            return;
        }
        
        terminated = true;
        
        try {
            // Close I/O streams
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    logger.debug("Error closing writer for process {}", processId, e);
                }
            }
            
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.debug("Error closing reader for process {}", processId, e);
                }
            }
            
            // Terminate the process
            if (process != null && process.isAlive()) {
                try {
                    // Try graceful termination first
                    process.destroy();
                    
                    // Wait up to 5 seconds for graceful termination
                    if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                        logger.warn("Process {} did not terminate gracefully, forcing termination", processId);
                        process.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting for process {} to terminate", processId);
                    process.destroyForcibly();
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("Error terminating process {}", processId, e);
                    process.destroyForcibly();
                }
            }
            
            logger.debug("Successfully terminated managed process {}", processId);
            
        } catch (Exception e) {
            logger.error("Error during process termination for {}", processId, e);
        }
    }
    
    @Override
    public String toString() {
        return "ManagedProcess{" +
                "processId='" + processId + '\'' +
                ", username='" + username + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", createdAt=" + createdAt +
                ", lastAccessedAt=" + lastAccessedAt +
                ", healthy=" + isHealthy() +
                ", idle=" + isIdle() +
                ", uptimeSeconds=" + getUptimeSeconds() +
                '}';
    }
}