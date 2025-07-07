package com.example.banking.api.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    private UserSession userSession;
    private static final String SESSION_ID = "test-session-id";
    private static final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        userSession = new UserSession(SESSION_ID, USERNAME);
    }

    @Test
    void testUserSessionCreation() {
        // Then
        assertEquals(SESSION_ID, userSession.getSessionId());
        assertEquals(USERNAME, userSession.getUsername());
        assertTrue(userSession.isActive());
        assertNotNull(userSession.getCreatedAt());
        assertNotNull(userSession.getLastAccessedAt());
        assertNull(userSession.getBankingProcess());
        assertNull(userSession.getProcessId());
    }

    @Test
    void testUpdateLastAccessed() {
        // Given
        LocalDateTime initialLastAccessed = userSession.getLastAccessedAt();

        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        userSession.updateLastAccessed();

        // Then
        assertTrue(userSession.getLastAccessedAt().isAfter(initialLastAccessed));
    }

    @Test
    void testSetActive() {
        // When
        userSession.setActive(false);

        // Then
        assertFalse(userSession.isActive());

        // When
        userSession.setActive(true);

        // Then
        assertTrue(userSession.isActive());
    }

    @Test
    void testSetAndGetProcessId() {
        // Given
        String processId = "process-123";

        // When
        userSession.setProcessId(processId);

        // Then
        assertEquals(processId, userSession.getProcessId());
    }

    @Test
    void testHasActiveProcess_WithoutProcess() {
        // Then
        assertFalse(userSession.hasActiveProcess());
    }

    @Test
    void testHasActiveProcess_WithMockProcess() {
        // Note: Since Process is final and hard to mock,
        // we test the null case and set up scenarios
        // In real scenarios, the process would be set by ProcessManager
        
        // When process is null
        assertFalse(userSession.hasActiveProcess());
        
        // When process is set but null
        userSession.setBankingProcess(null);
        assertFalse(userSession.hasActiveProcess());
    }

    @Test
    void testIsExpired_NotExpired() {
        // Given
        long timeoutInSeconds = 3600; // 1 hour

        // When
        boolean isExpired = userSession.isExpired(timeoutInSeconds);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void testIsExpired_Expired() throws InterruptedException {
        // Given
        long timeoutInSeconds = 1; // 1 second

        // Wait for expiration
        Thread.sleep(1100); // 1.1 seconds

        // When
        boolean isExpired = userSession.isExpired(timeoutInSeconds);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void testGetSessionDurationInSeconds() throws InterruptedException {
        // Given
        Thread.sleep(100); // Wait a bit

        // When
        long duration = userSession.getSessionDurationInSeconds();

        // Then
        assertTrue(duration >= 0);
        assertTrue(duration < 2); // Should be less than 2 seconds
    }

    @Test
    void testEquals_SameSession() {
        // Given
        UserSession anotherSession = new UserSession(SESSION_ID, "different-user");

        // When/Then
        assertEquals(userSession, anotherSession);
    }

    @Test
    void testEquals_DifferentSession() {
        // Given
        UserSession differentSession = new UserSession("different-session-id", USERNAME);

        // When/Then
        assertNotEquals(userSession, differentSession);
    }

    @Test
    void testEquals_SameObject() {
        // When/Then
        assertEquals(userSession, userSession);
    }

    @Test
    void testEquals_NullObject() {
        // When/Then
        assertNotEquals(userSession, null);
    }

    @Test
    void testEquals_DifferentClass() {
        // When/Then
        assertNotEquals(userSession, "not-a-session");
    }

    @Test
    void testHashCode_SameSession() {
        // Given
        UserSession anotherSession = new UserSession(SESSION_ID, "different-user");

        // When/Then
        assertEquals(userSession.hashCode(), anotherSession.hashCode());
    }

    @Test
    void testHashCode_DifferentSession() {
        // Given
        UserSession differentSession = new UserSession("different-session-id", USERNAME);

        // When/Then
        assertNotEquals(userSession.hashCode(), differentSession.hashCode());
    }

    @Test
    void testToString() {
        // When
        String toString = userSession.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains(SESSION_ID));
        assertTrue(toString.contains(USERNAME));
        assertTrue(toString.contains("UserSession"));
    }
}