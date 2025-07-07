package com.example.banking.api.service.session;

import com.example.banking.api.domain.model.UserSession;
import com.example.banking.api.config.BankingApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpSession;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Collection;
import java.util.Optional;

@Service
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static final String USER_SESSION_ATTRIBUTE = "USER_SESSION";
    
    private final ConcurrentHashMap<String, UserSession> activeSessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    @Value("${banking.session.max-concurrent-sessions:100}")
    private int maxConcurrentSessions;
    
    @Value("${banking.session.process-idle-timeout:300000}")
    private long processIdleTimeoutMs;
    
    @Value("${banking.session.cleanup-interval:60000}")
    private long cleanupIntervalMs;
    
    @Autowired
    private BankingApplicationProperties bankingProperties;
    
    public SessionManager() {
        // Schedule cleanup task - will be initialized after Spring sets the values
    }
    
    @PostConstruct
    public void initializeCleanupTask() {
        // Schedule cleanup task after all values are injected
        cleanupExecutor.scheduleAtFixedRate(
            this::cleanupExpiredSessions, 
            cleanupIntervalMs, 
            cleanupIntervalMs, 
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Creates a new user session
     */
    public UserSession createSession(String username, HttpSession httpSession) {
        if (activeSessions.size() >= maxConcurrentSessions) {
            throw new IllegalStateException("Maximum concurrent sessions exceeded");
        }
        
        String sessionId = httpSession.getId();
        UserSession userSession = new UserSession(sessionId, username);
        
        activeSessions.put(sessionId, userSession);
        httpSession.setAttribute(USER_SESSION_ATTRIBUTE, userSession);
        
        logger.info("Created session for user: {} with sessionId: {}", username, sessionId);
        return userSession;
    }
    
    /**
     * Retrieves a user session by session ID
     */
    public Optional<UserSession> getSession(String sessionId) {
        UserSession session = activeSessions.get(sessionId);
        if (session != null && session.isActive()) {
            session.updateLastAccessed();
            return Optional.of(session);
        }
        return Optional.empty();
    }
    
    /**
     * Retrieves a user session from HTTP session
     */
    public Optional<UserSession> getSession(HttpSession httpSession) {
        UserSession session = (UserSession) httpSession.getAttribute(USER_SESSION_ATTRIBUTE);
        if (session != null && session.isActive()) {
            session.updateLastAccessed();
            return Optional.of(session);
        }
        return Optional.empty();
    }
    
    /**
     * Invalidates a user session
     */
    public void invalidateSession(String sessionId) {
        UserSession session = activeSessions.remove(sessionId);
        if (session != null) {
            session.setActive(false);
            cleanupSessionProcess(session);
            logger.info("Invalidated session for user: {} with sessionId: {}", 
                       session.getUsername(), sessionId);
        }
    }
    
    /**
     * Invalidates a user session from HTTP session
     */
    public void invalidateSession(HttpSession httpSession) {
        UserSession session = (UserSession) httpSession.getAttribute(USER_SESSION_ATTRIBUTE);
        if (session != null) {
            invalidateSession(session.getSessionId());
        }
        try {
            httpSession.invalidate();
        } catch (IllegalStateException e) {
            logger.debug("HTTP session already invalidated: {}", e.getMessage());
        }
    }
    
    /**
     * Checks if a session is valid and active
     */
    public boolean isSessionValid(String sessionId) {
        return getSession(sessionId).isPresent();
    }
    
    /**
     * Gets the total number of active sessions
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * Gets all active sessions (for monitoring purposes)
     */
    public Collection<UserSession> getActiveSessions() {
        return activeSessions.values();
    }
    
    /**
     * Updates session activity
     */
    public void updateSessionActivity(String sessionId) {
        UserSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.updateLastAccessed();
        }
    }
    
    /**
     * Cleanup expired sessions
     */
    private void cleanupExpiredSessions() {
        long timeoutInSeconds = processIdleTimeoutMs / 1000;
        
        activeSessions.entrySet().removeIf(entry -> {
            UserSession session = entry.getValue();
            if (session.isExpired(timeoutInSeconds)) {
                logger.info("Cleaning up expired session for user: {} with sessionId: {}", 
                           session.getUsername(), session.getSessionId());
                session.setActive(false);
                cleanupSessionProcess(session);
                return true;
            }
            return false;
        });
    }
    
    /**
     * Cleanup process associated with a session
     */
    private void cleanupSessionProcess(UserSession session) {
        if (session.hasActiveProcess()) {
            try {
                Process process = session.getBankingProcess();
                if (process.isAlive()) {
                    process.destroyForcibly();
                    logger.info("Terminated process for session: {}", session.getSessionId());
                }
            } catch (Exception e) {
                logger.error("Error terminating process for session: {}", session.getSessionId(), e);
            }
        }
    }
    
    /**
     * Shutdown the session manager
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down SessionManager");
        cleanupExecutor.shutdown();
        
        // Cleanup all active sessions
        activeSessions.values().forEach(this::cleanupSessionProcess);
        activeSessions.clear();
        
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}