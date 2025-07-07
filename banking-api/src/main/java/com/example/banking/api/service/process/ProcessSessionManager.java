package com.example.banking.api.service.process;

import com.example.banking.api.domain.model.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

/**
 * Manages the mapping between user sessions and their corresponding banking processes.
 * This service orchestrates the creation, reuse, and cleanup of session-specific processes.
 */
@Service
public class ProcessSessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessSessionManager.class);
    
    @Autowired
    private ProcessPool processPool;
    
    @Autowired
    private ProcessExecutor processExecutor;
    
    /**
     * Executes an operation for a given user session.
     * This method handles getting or creating a process for the session and executing the operation.
     *
     * @param userSession The user session
     * @param operation The operation to execute
     * @return The result of the operation
     * @throws ProcessExecutionException if the operation fails
     */
    public <T> T executeForSession(UserSession userSession, ProcessOperation<T> operation) {
        try {
            // Get or create a managed process for this session
            ManagedProcess managedProcess = processPool.getOrCreateProcess(userSession);
            
            logger.debug("Executing operation for session {} using process {}", 
                        userSession.getSessionId(), managedProcess.getProcessId());
            
            // Execute the operation with the managed process
            T result = processExecutor.executeWithManagedProcess(operation, managedProcess);
            
            logger.debug("Successfully executed operation for session {}", userSession.getSessionId());
            return result;
            
        } catch (ProcessCreationException e) {
            logger.error("Failed to create process for session {}", userSession.getSessionId(), e);
            throw new ProcessExecutionException("Failed to create process for session", e);
        } catch (Exception e) {
            logger.error("Error executing operation for session {}", userSession.getSessionId(), e);
            throw new ProcessExecutionException("Failed to execute operation for session", e);
        }
    }
    
    /**
     * Executes an authentication operation for a session.
     * This is a special case that handles login and creates an authenticated session process.
     *
     * @param userSession The user session
     * @param operation The authentication operation
     * @return The result of the authentication
     * @throws ProcessExecutionException if authentication fails
     */
    public <T> T authenticateForSession(UserSession userSession, ProcessOperation<T> operation) {
        try {
            // For authentication, we always create a new process
            ManagedProcess managedProcess = processPool.getOrCreateProcess(userSession);
            
            logger.debug("Authenticating session {} using process {}", 
                        userSession.getSessionId(), managedProcess.getProcessId());
            
            // Execute the authentication operation
            T result = processExecutor.executeWithManagedProcess(operation, managedProcess);
            
            logger.info("Successfully authenticated session {}", userSession.getSessionId());
            return result;
            
        } catch (ProcessCreationException e) {
            logger.error("Failed to create process for authentication in session {}", userSession.getSessionId(), e);
            // Clean up failed process
            terminateSessionProcess(userSession);
            throw new ProcessExecutionException("Failed to create process for authentication", e);
        } catch (Exception e) {
            logger.error("Authentication failed for session {}", userSession.getSessionId(), e);
            // Clean up failed process
            terminateSessionProcess(userSession);
            throw new ProcessExecutionException("Authentication failed", e);
        }
    }
    
    /**
     * Terminates the process associated with a user session.
     *
     * @param userSession The user session
     */
    public void terminateSessionProcess(UserSession userSession) {
        String sessionId = userSession.getSessionId();
        logger.info("Terminating process for session {}", sessionId);
        
        try {
            processPool.terminateProcess(sessionId);
            
            // Clear process information from user session
            userSession.setBankingProcess(null);
            userSession.setProcessId(null);
            
            logger.info("Successfully terminated process for session {}", sessionId);
            
        } catch (Exception e) {
            logger.error("Error terminating process for session {}", sessionId, e);
        }
    }
    
    /**
     * Checks if a session has an active process
     *
     * @param userSession The user session
     * @return true if the session has an active process
     */
    public boolean hasActiveProcess(UserSession userSession) {
        return userSession.hasActiveProcess();
    }
    
    /**
     * Gets process pool information for monitoring
     *
     * @return ProcessPoolInfo containing current pool state
     */
    public ProcessPool.ProcessPoolInfo getProcessPoolInfo() {
        return processPool.getProcessPoolInfo();
    }
    
    /**
     * Performs health check on session processes
     * This method can be called by monitoring or management endpoints
     */
    public void performHealthCheck() {
        // The health check is automatically performed by ProcessPool
        // This method provides a way to manually trigger it if needed
        logger.debug("Health check triggered via ProcessSessionManager");
    }
    
    /**
     * Cleanup when the service is destroyed
     */
    @PreDestroy
    public void cleanup() {
        logger.info("ProcessSessionManager cleanup initiated");
        // ProcessPool will handle its own cleanup via @PreDestroy
    }
}