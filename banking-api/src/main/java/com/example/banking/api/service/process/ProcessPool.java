package com.example.banking.api.service.process;

import com.example.banking.api.config.BankingApplicationProperties;
import com.example.banking.api.domain.model.UserSession;
import com.example.banking.api.service.JarLocatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.*;
import java.util.concurrent.*;
import java.util.Map;
import java.util.UUID;

/**
 * Manages a pool of persistent banking processes for session-based operations.
 * Each user session gets its own dedicated process that stays alive for the duration of the session.
 */
@Service
public class ProcessPool {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessPool.class);
    
    private final Map<String, ManagedProcess> sessionProcesses = new ConcurrentHashMap<>();
    private final ScheduledExecutorService healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService processExecutor = Executors.newCachedThreadPool();
    
    @Autowired
    private BankingApplicationProperties properties;
    
    @Autowired
    private JarLocatorService jarLocatorService;
    
    @Value("${banking.session.process-idle-timeout:300000}")
    private long processIdleTimeoutMs;
    
    @Value("${banking.session.cleanup-interval:60000}")
    private long healthCheckIntervalMs;
    
    public ProcessPool() {
        // Health check task will be scheduled after Spring injects values
    }
    
    @PostConstruct
    public void initializeHealthCheck() {
        // Schedule health check task after all values are injected
        healthCheckExecutor.scheduleAtFixedRate(
            this::performHealthCheck, 
            healthCheckIntervalMs, 
            healthCheckIntervalMs, 
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Gets or creates a persistent process for the given user session
     */
    public ManagedProcess getOrCreateProcess(UserSession userSession) throws ProcessCreationException {
        String sessionId = userSession.getSessionId();
        
        ManagedProcess managedProcess = sessionProcesses.get(sessionId);
        
        if (managedProcess == null || !managedProcess.isHealthy()) {
            if (managedProcess != null) {
                logger.info("Existing process for session {} is unhealthy, creating new one", sessionId);
                terminateProcess(sessionId);
            }
            
            managedProcess = createNewProcess(userSession);
            sessionProcesses.put(sessionId, managedProcess);
            
            // Update user session with process information
            userSession.setBankingProcess(managedProcess.getProcess());
            userSession.setProcessId(managedProcess.getProcessId());
            
            logger.info("Created new process {} for session {}", managedProcess.getProcessId(), sessionId);
        } else {
            // Update last accessed time
            managedProcess.updateLastAccessed();
            userSession.updateLastAccessed();
        }
        
        return managedProcess;
    }
    
    /**
     * Creates a new managed process for a user session
     */
    private ManagedProcess createNewProcess(UserSession userSession) throws ProcessCreationException {
        try {
            String jarPath = jarLocatorService.getJarPath();
            
            if (jarPath == null || !jarLocatorService.isJarAccessible()) {
                throw new ProcessCreationException("Banking application JAR not accessible: " + jarLocatorService.getJarInfo());
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
            
            Process process = processBuilder.start();
            
            // Create I/O wrappers
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String processId = UUID.randomUUID().toString();
            ManagedProcess managedProcess = new ManagedProcess(
                processId, 
                process, 
                writer, 
                reader, 
                userSession.getUsername(),
                userSession.getSessionId()
            );
            
            return managedProcess;
            
        } catch (IOException e) {
            logger.error("Failed to create process for session {}", userSession.getSessionId(), e);
            throw new ProcessCreationException("Failed to create banking process", e);
        }
    }
    
    /**
     * Terminates a process for the given session
     */
    public void terminateProcess(String sessionId) {
        ManagedProcess managedProcess = sessionProcesses.remove(sessionId);
        if (managedProcess != null) {
            try {
                managedProcess.terminate();
                logger.info("Terminated process {} for session {}", managedProcess.getProcessId(), sessionId);
            } catch (Exception e) {
                logger.error("Error terminating process {} for session {}", managedProcess.getProcessId(), sessionId, e);
            }
        }
    }
    
    /**
     * Gets process information for monitoring
     */
    public ProcessPoolInfo getProcessPoolInfo() {
        return new ProcessPoolInfo(
            sessionProcesses.size(),
            sessionProcesses.values().stream()
                .mapToInt(p -> p.isHealthy() ? 1 : 0)
                .sum(),
            sessionProcesses.values().stream()
                .filter(ManagedProcess::isIdle)
                .mapToInt(p -> 1)
                .sum()
        );
    }
    
    /**
     * Performs health check on all managed processes
     */
    private void performHealthCheck() {
        logger.debug("Performing health check on {} processes", sessionProcesses.size());
        
        sessionProcesses.entrySet().removeIf(entry -> {
            String sessionId = entry.getKey();
            ManagedProcess managedProcess = entry.getValue();
            
            // Check if process is dead
            if (!managedProcess.isHealthy()) {
                logger.info("Removing dead process {} for session {}", managedProcess.getProcessId(), sessionId);
                try {
                    managedProcess.terminate();
                } catch (Exception e) {
                    logger.error("Error terminating dead process", e);
                }
                return true;
            }
            
            // Check if process is idle for too long
            if (managedProcess.isIdleTimeout(processIdleTimeoutMs)) {
                logger.info("Removing idle process {} for session {} (idle for {}ms)", 
                           managedProcess.getProcessId(), sessionId, 
                           System.currentTimeMillis() - managedProcess.getLastAccessedTime());
                try {
                    managedProcess.terminate();
                } catch (Exception e) {
                    logger.error("Error terminating idle process", e);
                }
                return true;
            }
            
            return false;
        });
        
        logger.debug("Health check completed. Active processes: {}", sessionProcesses.size());
    }
    
    /**
     * Shutdown the process pool
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down ProcessPool");
        
        // Stop health check
        healthCheckExecutor.shutdown();
        
        // Terminate all processes
        sessionProcesses.forEach((sessionId, process) -> {
            try {
                process.terminate();
                logger.info("Terminated process {} for session {}", process.getProcessId(), sessionId);
            } catch (Exception e) {
                logger.error("Error terminating process during shutdown", e);
            }
        });
        
        sessionProcesses.clear();
        
        // Shutdown executors
        try {
            if (!healthCheckExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                healthCheckExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            healthCheckExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        processExecutor.shutdown();
        try {
            if (!processExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                processExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            processExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Information about the process pool state
     */
    public static class ProcessPoolInfo {
        private final int totalProcesses;
        private final int healthyProcesses;
        private final int idleProcesses;
        
        public ProcessPoolInfo(int totalProcesses, int healthyProcesses, int idleProcesses) {
            this.totalProcesses = totalProcesses;
            this.healthyProcesses = healthyProcesses;
            this.idleProcesses = idleProcesses;
        }
        
        public int getTotalProcesses() { return totalProcesses; }
        public int getHealthyProcesses() { return healthyProcesses; }
        public int getIdleProcesses() { return idleProcesses; }
        
        @Override
        public String toString() {
            return String.format("ProcessPoolInfo{total=%d, healthy=%d, idle=%d}", 
                               totalProcesses, healthyProcesses, idleProcesses);
        }
    }
}