package com.example.banking.api.service.process;

import com.example.banking.api.domain.model.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ProcessSessionManagerTest {

    @Mock
    private ProcessPool processPool;

    @Mock
    private ProcessExecutor processExecutor;

    @Mock
    private UserSession userSession;

    @Mock
    private ManagedProcess managedProcess;

    @Mock
    private ProcessOperation<String> operation;

    @InjectMocks
    private ProcessSessionManager processSessionManager;

    private static final String SESSION_ID = "test-session-id";
    private static final String USERNAME = "testuser";
    private static final String PROCESS_ID = "test-process-id";

    @BeforeEach
    void setUp() {
        // Use lenient stubbing to avoid unnecessary stubbing warnings
        lenient().when(userSession.getSessionId()).thenReturn(SESSION_ID);
        lenient().when(userSession.getUsername()).thenReturn(USERNAME);
        lenient().when(managedProcess.getProcessId()).thenReturn(PROCESS_ID);
        lenient().when(managedProcess.isHealthy()).thenReturn(true);
    }

    @Test
    void testExecuteForSession_Success() throws Exception {
        // Given
        String expectedResult = "operation-result";
        when(processPool.getOrCreateProcess(userSession)).thenReturn(managedProcess);
        when(processExecutor.executeWithManagedProcess(operation, managedProcess)).thenReturn(expectedResult);

        // When
        String result = processSessionManager.executeForSession(userSession, operation);

        // Then
        assertEquals(expectedResult, result);
        verify(processPool).getOrCreateProcess(userSession);
        verify(processExecutor).executeWithManagedProcess(operation, managedProcess);
    }

    @Test
    void testExecuteForSession_ProcessCreationException() throws Exception {
        // Given
        ProcessCreationException creationException = new ProcessCreationException("Failed to create process");
        when(processPool.getOrCreateProcess(userSession)).thenThrow(creationException);

        // When/Then
        ProcessExecutionException exception = assertThrows(ProcessExecutionException.class, () -> {
            processSessionManager.executeForSession(userSession, operation);
        });

        assertEquals("Failed to create process for session", exception.getMessage());
        assertEquals(creationException, exception.getCause());
    }

    @Test
    void testExecuteForSession_ExecutionException() throws Exception {
        // Given
        RuntimeException executionException = new RuntimeException("Execution failed");
        when(processPool.getOrCreateProcess(userSession)).thenReturn(managedProcess);
        when(processExecutor.executeWithManagedProcess(operation, managedProcess)).thenThrow(executionException);

        // When/Then
        ProcessExecutionException exception = assertThrows(ProcessExecutionException.class, () -> {
            processSessionManager.executeForSession(userSession, operation);
        });

        assertEquals("Failed to execute operation for session", exception.getMessage());
        assertEquals(executionException, exception.getCause());
    }

    @Test
    void testAuthenticateForSession_Success() throws Exception {
        // Given
        String expectedResult = "auth-result";
        when(processPool.getOrCreateProcess(userSession)).thenReturn(managedProcess);
        when(processExecutor.executeWithManagedProcess(operation, managedProcess)).thenReturn(expectedResult);

        // When
        String result = processSessionManager.authenticateForSession(userSession, operation);

        // Then
        assertEquals(expectedResult, result);
        verify(processPool).getOrCreateProcess(userSession);
        verify(processExecutor).executeWithManagedProcess(operation, managedProcess);
    }

    @Test
    void testAuthenticateForSession_ProcessCreationException() throws Exception {
        // Given
        ProcessCreationException creationException = new ProcessCreationException("Failed to create process");
        when(processPool.getOrCreateProcess(userSession)).thenThrow(creationException);

        // When/Then
        ProcessExecutionException exception = assertThrows(ProcessExecutionException.class, () -> {
            processSessionManager.authenticateForSession(userSession, operation);
        });

        assertEquals("Failed to create process for authentication", exception.getMessage());
        assertEquals(creationException, exception.getCause());
        verify(processPool).terminateProcess(SESSION_ID);
    }

    @Test
    void testAuthenticateForSession_AuthenticationFailure() throws Exception {
        // Given
        RuntimeException authException = new RuntimeException("Authentication failed");
        when(processPool.getOrCreateProcess(userSession)).thenReturn(managedProcess);
        when(processExecutor.executeWithManagedProcess(operation, managedProcess)).thenThrow(authException);

        // When/Then
        ProcessExecutionException exception = assertThrows(ProcessExecutionException.class, () -> {
            processSessionManager.authenticateForSession(userSession, operation);
        });

        assertEquals("Authentication failed", exception.getMessage());
        assertEquals(authException, exception.getCause());
        verify(processPool).terminateProcess(SESSION_ID);
    }

    @Test
    void testTerminateSessionProcess() {
        // When
        processSessionManager.terminateSessionProcess(userSession);

        // Then
        verify(processPool).terminateProcess(SESSION_ID);
        verify(userSession).setBankingProcess(null);
        verify(userSession).setProcessId(null);
    }

    @Test
    void testTerminateSessionProcess_WithException() {
        // Given
        doThrow(new RuntimeException("Termination failed")).when(processPool).terminateProcess(SESSION_ID);

        // When
        processSessionManager.terminateSessionProcess(userSession); // Should not throw

        // Then
        verify(processPool).terminateProcess(SESSION_ID);
        // Should continue execution despite exception
    }

    @Test
    void testHasActiveProcess_True() {
        // Given
        when(userSession.hasActiveProcess()).thenReturn(true);

        // When
        boolean hasActive = processSessionManager.hasActiveProcess(userSession);

        // Then
        assertTrue(hasActive);
        verify(userSession).hasActiveProcess();
    }

    @Test
    void testHasActiveProcess_False() {
        // Given
        when(userSession.hasActiveProcess()).thenReturn(false);

        // When
        boolean hasActive = processSessionManager.hasActiveProcess(userSession);

        // Then
        assertFalse(hasActive);
        verify(userSession).hasActiveProcess();
    }

    @Test
    void testGetProcessPoolInfo() {
        // Given
        ProcessPool.ProcessPoolInfo expectedInfo = new ProcessPool.ProcessPoolInfo(5, 4, 2);
        when(processPool.getProcessPoolInfo()).thenReturn(expectedInfo);

        // When
        ProcessPool.ProcessPoolInfo info = processSessionManager.getProcessPoolInfo();

        // Then
        assertEquals(expectedInfo, info);
        verify(processPool).getProcessPoolInfo();
    }

    @Test
    void testPerformHealthCheck() {
        // When
        processSessionManager.performHealthCheck();

        // Then
        // No exception should be thrown
        // This method mainly logs debug information
    }
}