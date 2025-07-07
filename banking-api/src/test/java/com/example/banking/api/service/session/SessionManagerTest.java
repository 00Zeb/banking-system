package com.example.banking.api.service.session;

import com.example.banking.api.domain.model.UserSession;
import com.example.banking.api.config.BankingApplicationProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionManagerTest {

    @Mock
    private HttpSession httpSession;

    @Mock
    private BankingApplicationProperties bankingProperties;

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        // Create SessionManager instance manually to avoid constructor issues
        sessionManager = new SessionManager();
        
        // Set configuration values using reflection
        ReflectionTestUtils.setField(sessionManager, "maxConcurrentSessions", 10);
        ReflectionTestUtils.setField(sessionManager, "processIdleTimeoutMs", 60000L);
        ReflectionTestUtils.setField(sessionManager, "cleanupIntervalMs", 10000L);
        ReflectionTestUtils.setField(sessionManager, "bankingProperties", bankingProperties);
        
        // Initialize cleanup task
        sessionManager.initializeCleanupTask();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up after each test
        if (sessionManager != null) {
            sessionManager.shutdown();
        }
    }

    @Test
    void testCreateSession_Success() {
        // Given
        String sessionId = "test-session-id";
        String username = "testuser";
        when(httpSession.getId()).thenReturn(sessionId);

        // When
        UserSession userSession = sessionManager.createSession(username, httpSession);

        // Then
        assertNotNull(userSession);
        assertEquals(sessionId, userSession.getSessionId());
        assertEquals(username, userSession.getUsername());
        assertTrue(userSession.isActive());
        verify(httpSession).setAttribute("USER_SESSION", userSession);
    }

    @Test
    void testCreateSession_ExceedsMaxConcurrentSessions() {
        // Given
        ReflectionTestUtils.setField(sessionManager, "maxConcurrentSessions", 1);
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";
        String username = "testuser";

        when(httpSession.getId()).thenReturn(sessionId1, sessionId2);

        // Create first session
        sessionManager.createSession(username, httpSession);

        // When/Then
        assertThrows(IllegalStateException.class, () -> {
            sessionManager.createSession(username, httpSession);
        });
    }

    @Test
    void testGetSession_BySessionId_Success() {
        // Given
        String sessionId = "test-session-id";
        String username = "testuser";
        when(httpSession.getId()).thenReturn(sessionId);

        UserSession createdSession = sessionManager.createSession(username, httpSession);

        // When
        Optional<UserSession> retrievedSession = sessionManager.getSession(sessionId);

        // Then
        assertTrue(retrievedSession.isPresent());
        assertEquals(createdSession.getSessionId(), retrievedSession.get().getSessionId());
        assertEquals(createdSession.getUsername(), retrievedSession.get().getUsername());
    }

    @Test
    void testGetSession_BySessionId_NotFound() {
        // When
        Optional<UserSession> session = sessionManager.getSession("non-existent-id");

        // Then
        assertFalse(session.isPresent());
    }

    @Test
    void testGetSession_ByHttpSession_Success() {
        // Given
        String sessionId = "test-session-id";
        String username = "testuser";
        when(httpSession.getId()).thenReturn(sessionId);

        UserSession createdSession = sessionManager.createSession(username, httpSession);
        when(httpSession.getAttribute("USER_SESSION")).thenReturn(createdSession);

        // When
        Optional<UserSession> retrievedSession = sessionManager.getSession(httpSession);

        // Then
        assertTrue(retrievedSession.isPresent());
        assertEquals(createdSession.getSessionId(), retrievedSession.get().getSessionId());
    }

    @Test
    void testGetSession_ByHttpSession_NotFound() {
        // Given
        when(httpSession.getAttribute("USER_SESSION")).thenReturn(null);

        // When
        Optional<UserSession> session = sessionManager.getSession(httpSession);

        // Then
        assertFalse(session.isPresent());
    }

    @Test
    void testInvalidateSession_BySessionId() {
        // Given
        String sessionId = "test-session-id";
        String username = "testuser";
        when(httpSession.getId()).thenReturn(sessionId);

        UserSession createdSession = sessionManager.createSession(username, httpSession);
        assertTrue(createdSession.isActive());

        // When
        sessionManager.invalidateSession(sessionId);

        // Then
        assertFalse(createdSession.isActive());
        Optional<UserSession> retrievedSession = sessionManager.getSession(sessionId);
        assertFalse(retrievedSession.isPresent());
    }

    @Test
    void testInvalidateSession_ByHttpSession() {
        // Given
        String sessionId = "test-session-id";
        String username = "testuser";
        when(httpSession.getId()).thenReturn(sessionId);

        UserSession createdSession = sessionManager.createSession(username, httpSession);
        when(httpSession.getAttribute("USER_SESSION")).thenReturn(createdSession);

        // When
        sessionManager.invalidateSession(httpSession);

        // Then
        assertFalse(createdSession.isActive());
        verify(httpSession).invalidate();
    }

    @Test
    void testIsSessionValid_ValidSession() {
        // Given
        String sessionId = "test-session-id";
        String username = "testuser";
        when(httpSession.getId()).thenReturn(sessionId);

        sessionManager.createSession(username, httpSession);

        // When
        boolean isValid = sessionManager.isSessionValid(sessionId);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testIsSessionValid_InvalidSession() {
        // When
        boolean isValid = sessionManager.isSessionValid("non-existent-id");

        // Then
        assertFalse(isValid);
    }

    @Test
    void testGetActiveSessionCount() {
        // Given
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";
        String username = "testuser";
        
        when(httpSession.getId()).thenReturn(sessionId1, sessionId2);

        // Initially no sessions
        assertEquals(0, sessionManager.getActiveSessionCount());

        // Create sessions
        sessionManager.createSession(username, httpSession);
        assertEquals(1, sessionManager.getActiveSessionCount());

        sessionManager.createSession(username, httpSession);
        assertEquals(2, sessionManager.getActiveSessionCount());

        // Invalidate one session
        sessionManager.invalidateSession(sessionId1);
        assertEquals(1, sessionManager.getActiveSessionCount());
    }

    @Test
    void testUpdateSessionActivity() {
        // Given
        String sessionId = "test-session-id";
        String username = "testuser";
        when(httpSession.getId()).thenReturn(sessionId);

        UserSession createdSession = sessionManager.createSession(username, httpSession);
        var initialLastAccessed = createdSession.getLastAccessedAt();

        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        sessionManager.updateSessionActivity(sessionId);

        // Then
        assertTrue(createdSession.getLastAccessedAt().isAfter(initialLastAccessed));
    }

    @Test
    void testGetActiveSessions() {
        // Given
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";
        String username = "testuser";
        
        when(httpSession.getId()).thenReturn(sessionId1, sessionId2);

        // When
        assertEquals(0, sessionManager.getActiveSessions().size());

        sessionManager.createSession(username, httpSession);
        assertEquals(1, sessionManager.getActiveSessions().size());

        sessionManager.createSession("user2", httpSession);
        assertEquals(2, sessionManager.getActiveSessions().size());

        // Then
        var activeSessions = sessionManager.getActiveSessions();
        assertEquals(2, activeSessions.size());
        assertTrue(activeSessions.stream().anyMatch(s -> s.getUsername().equals("testuser")));
        assertTrue(activeSessions.stream().anyMatch(s -> s.getUsername().equals("user2")));
    }
}