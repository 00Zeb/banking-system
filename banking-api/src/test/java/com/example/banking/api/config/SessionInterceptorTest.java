package com.example.banking.api.config;

import com.example.banking.api.domain.model.UserSession;
import com.example.banking.api.service.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionInterceptorTest {

    @Mock
    private SessionManager sessionManager;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession httpSession;

    @Mock
    private UserSession userSession;

    @InjectMocks
    private SessionInterceptor sessionInterceptor;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        // Only set up response writer when needed
    }

    @Test
    void testPreHandle_LoginEndpoint_ShouldSkipValidation() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/banking/login");
        when(request.getMethod()).thenReturn("POST");

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(sessionManager, never()).getSession(any(HttpSession.class));
    }

    @Test
    void testPreHandle_RegisterEndpoint_ShouldSkipValidation() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/banking/register");
        when(request.getMethod()).thenReturn("POST");

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(sessionManager, never()).getSession(any(HttpSession.class));
    }

    @Test
    void testPreHandle_OptionsRequest_ShouldSkipValidation() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/banking/deposit");
        when(request.getMethod()).thenReturn("OPTIONS");

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(sessionManager, never()).getSession(any(HttpSession.class));
    }

    @Test
    void testPreHandle_HealthEndpoint_ShouldSkipValidation() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/banking/health");
        when(request.getMethod()).thenReturn("GET");

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(sessionManager, never()).getSession(any(HttpSession.class));
    }

    @Test
    void testPreHandle_SwaggerEndpoint_ShouldSkipValidation() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/swagger-ui.html");
        when(request.getMethod()).thenReturn("GET");

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(sessionManager, never()).getSession(any(HttpSession.class));
    }

    @Test
    void testPreHandle_NoHttpSession_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/banking/deposit");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(null);
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        assertTrue(responseWriter.toString().contains("No active session found"));
    }

    @Test
    void testPreHandle_NoUserSession_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/banking/deposit");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(httpSession);
        when(httpSession.getId()).thenReturn("test-session-id");
        when(sessionManager.getSession(httpSession)).thenReturn(Optional.empty());
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        assertTrue(responseWriter.toString().contains("Invalid or expired session"));
    }

    @Test
    void testPreHandle_ValidSession_ShouldAllowRequest() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/banking/deposit");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(httpSession);
        when(sessionManager.getSession(httpSession)).thenReturn(Optional.of(userSession));
        when(userSession.getUsername()).thenReturn("testuser");
        when(userSession.getSessionId()).thenReturn("test-session-id");

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(request).setAttribute("userSession", userSession);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void testPreHandle_ActuatorEndpoint_ShouldSkipValidation() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/actuator/health");
        when(request.getMethod()).thenReturn("GET");

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(sessionManager, never()).getSession(any(HttpSession.class));
    }

    @Test
    void testPreHandle_ApiDocsEndpoint_ShouldSkipValidation() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api-docs");
        when(request.getMethod()).thenReturn("GET");

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(sessionManager, never()).getSession(any(HttpSession.class));
    }

    @Test
    void testPreHandle_V3ApiDocsEndpoint_ShouldSkipValidation() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/v3/api-docs/swagger-config");
        when(request.getMethod()).thenReturn("GET");

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(sessionManager, never()).getSession(any(HttpSession.class));
    }

    @Test
    void testPreHandle_ErrorEndpoint_ShouldSkipValidation() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/error");
        when(request.getMethod()).thenReturn("GET");

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(sessionManager, never()).getSession(any(HttpSession.class));
    }

    @Test
    void testPreHandle_FaviconEndpoint_ShouldSkipValidation() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/favicon.ico");
        when(request.getMethod()).thenReturn("GET");

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(sessionManager, never()).getSession(any(HttpSession.class));
    }

    @Test
    void testPreHandle_WebjarsEndpoint_ShouldSkipValidation() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/webjars/swagger-ui/index.html");
        when(request.getMethod()).thenReturn("GET");

        // When
        boolean result = sessionInterceptor.preHandle(request, response, null);

        // Then
        assertTrue(result);
        verify(sessionManager, never()).getSession(any(HttpSession.class));
    }
}