package com.example.banking.api.service.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagedProcessTest {

    @Mock
    private Process process;

    @Mock
    private BufferedWriter writer;

    @Mock
    private BufferedReader reader;

    @Mock
    private ProcessExecutor processExecutor;

    private ManagedProcess managedProcess;
    private static final String PROCESS_ID = "test-process-id";
    private static final String USERNAME = "testuser";
    private static final String SESSION_ID = "test-session-id";

    @BeforeEach
    void setUp() {
        // Only set up basic stubs needed for constructor
        managedProcess = new ManagedProcess(PROCESS_ID, process, writer, reader, USERNAME, SESSION_ID);
    }

    @Test
    void testManagedProcessCreation() {
        // Then
        assertEquals(PROCESS_ID, managedProcess.getProcessId());
        assertEquals(USERNAME, managedProcess.getUsername());
        assertEquals(SESSION_ID, managedProcess.getSessionId());
        assertEquals(process, managedProcess.getProcess());
        assertEquals(writer, managedProcess.getWriter());
        assertEquals(reader, managedProcess.getReader());
        assertNotNull(managedProcess.getCreatedAt());
        assertNotNull(managedProcess.getLastAccessedAt());
    }

    @Test
    void testUpdateLastAccessed() {
        // Given
        LocalDateTime initialLastAccessed = managedProcess.getLastAccessedAt();
        long initialLastAccessedTime = managedProcess.getLastAccessedTime();

        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        managedProcess.updateLastAccessed();

        // Then
        assertTrue(managedProcess.getLastAccessedAt().isAfter(initialLastAccessed));
        assertTrue(managedProcess.getLastAccessedTime() > initialLastAccessedTime);
    }

    @Test
    void testIsHealthy_HealthyProcess() {
        // Given
        when(process.isAlive()).thenReturn(true);

        // When/Then
        assertTrue(managedProcess.isHealthy());
    }

    @Test
    void testIsHealthy_DeadProcess() {
        // Given
        when(process.isAlive()).thenReturn(false);

        // When/Then
        assertFalse(managedProcess.isHealthy());
    }

    @Test
    void testIsHealthy_TerminatedProcess() {
        // Given
        when(process.isAlive()).thenReturn(true);

        // When
        managedProcess.terminate();

        // Then
        assertFalse(managedProcess.isHealthy());
    }

    @Test
    void testIsIdle_NewProcess() {
        // When/Then
        assertFalse(managedProcess.isIdle());
    }

    @Test
    void testIsIdleTimeout_NotTimedOut() {
        // Given
        long timeout = 60000; // 1 minute

        // When/Then
        assertFalse(managedProcess.isIdleTimeout(timeout));
    }

    @Test
    void testIsIdleTimeout_TimedOut() throws InterruptedException {
        // Given
        long timeout = 100; // 100ms

        // Wait for timeout
        Thread.sleep(150);

        // When/Then
        assertTrue(managedProcess.isIdleTimeout(timeout));
    }

    @Test
    void testGetUptimeSeconds() throws InterruptedException {
        // Given
        Thread.sleep(100); // Wait a bit

        // When
        long uptime = managedProcess.getUptimeSeconds();

        // Then
        assertTrue(uptime >= 0);
        assertTrue(uptime < 2); // Should be less than 2 seconds
    }

    @Test
    void testCreateCommunication_HealthyProcess() {
        // Given
        when(process.isAlive()).thenReturn(true);

        // When
        ProcessCommunication communication = managedProcess.createCommunication(processExecutor);

        // Then
        assertNotNull(communication);
    }

    @Test
    void testCreateCommunication_UnhealthyProcess() {
        // Given
        when(process.isAlive()).thenReturn(false);

        // When/Then
        assertThrows(IllegalStateException.class, () -> {
            managedProcess.createCommunication(processExecutor);
        });
    }

    @Test
    void testTerminate_HealthyProcess() throws Exception {
        // Given
        when(process.isAlive()).thenReturn(true);
        when(process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)).thenReturn(true);

        // When
        managedProcess.terminate();

        // Then
        assertFalse(managedProcess.isHealthy());
        verify(writer).close();
        verify(reader).close();
        verify(process).destroy();
        verify(process).waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Test
    void testTerminate_ForceTermination() throws Exception {
        // Given
        when(process.isAlive()).thenReturn(true);
        when(process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)).thenReturn(false);

        // When
        managedProcess.terminate();

        // Then
        assertFalse(managedProcess.isHealthy());
        verify(process).destroy();
        verify(process).waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
        verify(process).destroyForcibly();
    }

    @Test
    void testTerminate_AlreadyTerminated() throws Exception {
        // Given
        managedProcess.terminate();

        // When
        managedProcess.terminate(); // Call again

        // Then
        assertFalse(managedProcess.isHealthy());
        // Should not throw exception or cause issues
    }

    @Test
    void testTerminate_WithIOException() throws Exception {
        // Given
        when(process.isAlive()).thenReturn(true);
        doThrow(new java.io.IOException("Test exception")).when(writer).close();

        // When
        managedProcess.terminate(); // Should not throw exception

        // Then
        assertFalse(managedProcess.isHealthy());
    }

    @Test
    void testTerminate_WithInterruptedException() throws Exception {
        // Given
        when(process.isAlive()).thenReturn(true);
        when(process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS))
            .thenThrow(new InterruptedException("Test interruption"));

        // When
        managedProcess.terminate();

        // Then
        assertFalse(managedProcess.isHealthy());
        verify(process).destroyForcibly();
    }

    @Test
    void testToString() {
        // When
        String toString = managedProcess.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains(PROCESS_ID));
        assertTrue(toString.contains(USERNAME));
        assertTrue(toString.contains(SESSION_ID));
        assertTrue(toString.contains("ManagedProcess"));
    }
}